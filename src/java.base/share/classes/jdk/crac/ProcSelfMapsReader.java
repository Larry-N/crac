/*
 * Copyright (c) 2017, 2021, Azul Systems, Inc. All rights reserved.
 * Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package jdk.crac;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProcSelfMapsReader {
    
    public List<MemoryRange> memoryRanges;

    public ProcSelfMapsReader() {
        memoryRanges = new ArrayList<>();
    }

    public void readMemoryRanges() {
        String procSelfMapsFilePath = "/proc/self/maps";
        try (BufferedReader br = new BufferedReader(new FileReader(procSelfMapsFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\s+");
                if (parts.length >= 1) {
                    String[] addressParts = parts[0].split("-");
                    if (addressParts.length == 2) {
                        try {
                            long startAddress = Long.parseLong(addressParts[0], 16);
                            long endAddress = Long.parseLong(addressParts[1], 16);
                            String permissions = parts[1];
                            MemoryRange memoryRange = new MemoryRange(startAddress, endAddress, permissions); 
                            memoryRanges.add(memoryRange);
                        } catch (NumberFormatException ex) {
                            System.out.printf("NumberFormatException for ranges %s - %s\n", addressParts[0], addressParts[1]);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isAddressWithinMemoryRanges(long address) {
        for (MemoryRange range : memoryRanges) {
            if (range.containsAddress(address)) {
                return true;
            }
        }
        return false;
    }

        public boolean isAddressBelongsToMEmory(long address) {
        for (MemoryRange range : memoryRanges) {
            if (range.containsAddress(address)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAddressAvaliableForRead(long address) {
        for (MemoryRange range : memoryRanges) {
            if (range.containsAddress(address)) {
                return ((range.isReadSet()));
            }
        }
        return false;
    }
public class MemoryRange {
    private final long startAddress;
    private final long endAddress;
    private final String permissions;

    public MemoryRange(long startAddress, long endAddress, String permissions) {
        this.startAddress = startAddress;
        this.endAddress = endAddress;
        this.permissions = permissions;
    }

    public long getStartAddress() {
        return startAddress;
    }

    public long getEndAddress() {
        return endAddress;
    }

    public boolean containsAddress(long address) {
        return address >= startAddress && address < endAddress;
    }

    public boolean isReadSet() {
        return permissions.charAt(0) == 'r';
    }

    public boolean isWriteSet() {
        return permissions.charAt(1) == 'w';
    }

    public boolean isExecSet() {
        return permissions.charAt(2) == 'x';
    }

    @Override
    public String toString() {
        return "[" + Long.toHexString(startAddress) + " - " + Long.toHexString(endAddress) + "]";
    }
}
}
