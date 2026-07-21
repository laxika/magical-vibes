package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Soulbond pairing helpers (CR 702.94): pair two unpaired creatures under the same controller,
 * and clear the link when either leaves / changes control / ceases to be a creature.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SoulbondSupport {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    public void pair(GameData gameData, Permanent a, Permanent b) {
        if (a == null || b == null || a.getId().equals(b.getId())) {
            return;
        }
        clearPairing(gameData, a);
        clearPairing(gameData, b);
        a.setPairedWithId(b.getId());
        b.setPairedWithId(a.getId());
        gameBroadcastService.logAndBroadcast(gameData,
                GameLog.builder().card(a.getCard()).text(" becomes paired with ").card(b.getCard()).text(".").build());
        log.info("Game {} - {} paired with {}", gameData.id, a.getCard().getName(), b.getCard().getName());
    }

    /** Clears this permanent's soulbond link and its partner's reciprocal link. */
    public void clearPairing(GameData gameData, Permanent permanent) {
        if (permanent == null || permanent.getPairedWithId() == null) {
            return;
        }
        UUID partnerId = permanent.getPairedWithId();
        permanent.setPairedWithId(null);
        Permanent partner = gameQueryService.findPermanentById(gameData, partnerId);
        if (partner != null && permanent.getId().equals(partner.getPairedWithId())) {
            partner.setPairedWithId(null);
        }
    }

    /**
     * Clears pairing on leave-the-battlefield / control-change. Safe to call when unpaired.
     */
    public void onPermanentLeavesOrChangesControl(GameData gameData, Permanent permanent) {
        clearPairing(gameData, permanent);
    }

    public boolean isUnpairedCreature(GameData gameData, Permanent permanent) {
        return permanent != null
                && permanent.getPairedWithId() == null
                && gameQueryService.isCreature(gameData, permanent);
    }

    public List<UUID> collectUnpairedPartnerIds(GameData gameData, Permanent source, UUID controllerId) {
        return gameData.playerBattlefields.getOrDefault(controllerId, List.of()).stream()
                .filter(p -> !p.getId().equals(source.getId()))
                .filter(p -> isUnpairedCreature(gameData, p))
                .map(Permanent::getId)
                .toList();
    }
}
