package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaPerTappedCreatureToUntapEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link PayManaPerTappedCreatureToUntapEffect} (Magnetic Mountain's each-upkeep trigger).
 * "That player" — the player whose upkeep it is — is the stack entry's target. Gathers that player's
 * tapped creatures matching the filter and, if they can afford at least one, prompts a multi-permanent
 * choice capped by how many they can pay for. Paying and untapping happen when the choice is answered
 * (see {@code MultiPermanentChoiceHandlerService}).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayManaPerTappedCreatureToUntapEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayManaPerTappedCreatureToUntapEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PayManaPerTappedCreatureToUntapEffect) effect;
        UUID actingPlayerId = entry.getTargetId();
        if (actingPlayerId == null) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(actingPlayerId);
        List<UUID> tappedMatchingIds = new ArrayList<>();
        if (battlefield != null) {
            for (Permanent perm : battlefield) {
                if (perm.isTapped()
                        && predicateEvaluationService.matchesPermanentPredicate(gameData, perm, e.creatureFilter())) {
                    tappedMatchingIds.add(perm.getId());
                }
            }
        }

        if (tappedMatchingIds.isEmpty()) {
            return;
        }

        ManaPool pool = gameData.playerManaPools.get(actingPlayerId);
        int available = pool != null ? pool.getTotal() : 0;
        int affordable = e.manaPerCreature() > 0 ? available / e.manaPerCreature() : tappedMatchingIds.size();
        int maxCount = Math.min(tappedMatchingIds.size(), affordable);

        if (maxCount <= 0) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(entry.getCard().getName()
                    + " triggers, but " + gameData.playerIdToName.get(actingPlayerId)
                    + " can't pay to untap any creatures."));
            return;
        }

        playerInputService.beginMultiPermanentChoice(gameData, actingPlayerId, tappedMatchingIds, maxCount,
                new MultiPermanentChoiceContext.PayManaPerCreatureUntap(actingPlayerId, e.manaPerCreature()),
                "Choose any number of tapped creatures to untap (pay {" + e.manaPerCreature() + "} each).");
    }
}
