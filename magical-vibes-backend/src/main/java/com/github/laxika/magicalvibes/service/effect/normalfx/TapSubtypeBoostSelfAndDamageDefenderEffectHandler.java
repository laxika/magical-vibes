package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TapSubtypeBoostSelfAndDamageDefenderEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TapSubtypeBoostSelfAndDamageDefenderEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TapSubtypeBoostSelfAndDamageDefenderEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TapSubtypeBoostSelfAndDamageDefenderEffect) effect;
        UUID controllerId = entry.getControllerId();
        UUID sourcePermanentId = entry.getSourcePermanentId();

        // Find all untapped creatures with the required subtype the controller controls
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        List<UUID> eligibleIds = new ArrayList<>();
        if (battlefield != null) {
            for (Permanent perm : battlefield) {
                if (!perm.isTapped()
                        && gameQueryService.isCreature(gameData, perm)
                        && gameQueryService.matchesPermanentPredicate(gameData, perm,
                                new PermanentHasSubtypePredicate(e.subtype()))) {
                    eligibleIds.add(perm.getId());
                }
            }
        }

        if (eligibleIds.isEmpty()) {
            String logEntry = entry.getCard().getName() + "'s attack ability finds no untapped " + e.subtype().getDisplayName() + " to tap.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} attack trigger: no eligible {} to tap", gameData.id, entry.getCard().getName(), e.subtype().getDisplayName());
            return;
        }

        gameData.pendingTapSubtypeBoostSourcePermanentId = sourcePermanentId;
        playerInputService.beginMultiPermanentChoice(gameData, controllerId, eligibleIds, eligibleIds.size(),
                "You may tap any number of untapped " + e.subtype().getDisplayName() + " you control.");
    }
}
