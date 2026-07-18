package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfLandDefendingPlayerControlsAndAssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link GainControlOfLandDefendingPlayerControlsAndAssignNoCombatDamageEffect}
 * (Orcish Squatters). The stack entry's {@code targetId} is the defending player and
 * {@code sourcePermanentId} the attacking creature. Presents a max-1 choice among the lands the
 * defending player controls; taking control (for as long as the source stays on the battlefield)
 * and the "assigns no combat damage" rider are applied in
 * {@code MultiPermanentChoiceHandlerService} when a land is chosen.
 */
@Component
@RequiredArgsConstructor
public class GainControlOfLandDefendingPlayerControlsAndAssignNoCombatDamageEffectHandler
        implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainControlOfLandDefendingPlayerControlsAndAssignNoCombatDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID defenderId = entry.getTargetId();
        UUID sourcePermanentId = entry.getSourcePermanentId();
        UUID controllerId = entry.getControllerId();

        if (defenderId == null || sourcePermanentId == null) {
            return;
        }

        // Per ruling: if you no longer control the source when this resolves, it does nothing.
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(entry.getCard(), "'s ability has no effect (source left the battlefield)."));
            return;
        }

        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
        List<UUID> validLandIds = new ArrayList<>();
        if (defenderBattlefield != null) {
            for (Permanent perm : defenderBattlefield) {
                if (perm.getCard().hasType(CardType.LAND)) {
                    validLandIds.add(perm.getId());
                }
            }
        }

        if (validLandIds.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().card(entry.getCard()).text("'s ability resolves, but " + gameData.playerIdToName.get(defenderId) + " controls no lands.").build());
            return;
        }

        playerInputService.beginMultiPermanentChoice(gameData, controllerId, validLandIds, 1,
                new MultiPermanentChoiceContext.GainControlOfLandAndAssignNoCombatDamage(sourcePermanentId),
                entry.getCard().getName() + "'s ability — Choose a land "
                        + gameData.playerIdToName.get(defenderId) + " controls to gain control of.");
    }
}
