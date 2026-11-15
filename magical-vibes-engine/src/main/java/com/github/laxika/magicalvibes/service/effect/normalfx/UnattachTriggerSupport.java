package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.rules.AttachmentEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Emits the {@link AttachmentEvent.EquipmentUnattached} event of an Equipment that became unattached from a permanent
 * (CR 701.3d), which its "whenever this Equipment becomes unattached from a permanent" abilities trigger on (Captain's
 * Hook, Grafted Wargear, Grafted Exoskeleton, Stitcher's Graft). The event is emitted as the Equipment becomes
 * unattached, so the ability is the Equipment's as it was just before (CR 603.10c), as the layers leave it: a
 * face-down Equipment, or one that has lost its abilities (CR 613.1f), has no such ability.
 */
@Component
public class UnattachTriggerSupport {

    public void triggerDestroyOnUnattachIfNeeded(GameData gameData, Permanent equipment,
                                                 UUID oldAttachedTo) {
        triggerDestroyOnUnattachIfNeeded(gameData, equipment, oldAttachedTo,
                gameData.findControllerOf(equipment));
    }

    /**
     * {@code equipment}, controlled by {@code controllerId} (its controller before a control change), became unattached
     * from {@code oldAttachedTo}; nothing happens when it was attached to nothing.
     */
    public void triggerDestroyOnUnattachIfNeeded(GameData gameData, Permanent equipment,
                                                 UUID oldAttachedTo, UUID controllerId) {
        if (oldAttachedTo == null || controllerId == null) {
            return;
        }
        gameData.rulesEvents.occur(new AttachmentEvent.EquipmentUnattached(equipment, oldAttachedTo, controllerId));
    }
}
