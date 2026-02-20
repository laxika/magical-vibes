package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.DraftData;
import com.github.laxika.magicalvibes.model.DraftStatus;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DraftRegistry {

    private final Map<UUID, DraftData> drafts = new ConcurrentHashMap<>();

    public void register(DraftData draftData) {
        drafts.put(draftData.id, draftData);
    }

    public DraftData get(UUID draftId) {
        return drafts.get(draftId);
    }

    public DraftData getDraftForPlayer(UUID userId) {
        for (DraftData d : drafts.values()) {
            if (d.playerIds.contains(userId) && d.status != DraftStatus.FINISHED) {
                return d;
            }
        }
        return null;
    }

    public void remove(UUID draftId) {
        drafts.remove(draftId);
    }
}

