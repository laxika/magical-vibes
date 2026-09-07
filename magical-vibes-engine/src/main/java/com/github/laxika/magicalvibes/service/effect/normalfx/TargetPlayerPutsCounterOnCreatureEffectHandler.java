package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerPutsCounterOnCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a targeted player's choice of a creature for counter placement. */
@Component
@RequiredArgsConstructor
public class TargetPlayerPutsCounterOnCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerPutsCounterOnCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TargetPlayerPutsCounterOnCreatureEffect counterEffect =
                (TargetPlayerPutsCounterOnCreatureEffect) effect;
        List<UUID> targets = entry.targetsForEffect(effect);
        UUID targetPlayerId = !targets.isEmpty() ? targets.getFirst() : entry.getTargetId();
        if (targetPlayerId == null) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield == null) {
            return;
        }
        List<UUID> creatureIds = battlefield.stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .map(Permanent::getId)
                .toList();
        if (creatureIds.isEmpty()) {
            return;
        }

        if (creatureIds.size() == 1) {
            Permanent target = gameQueryService.findPermanentById(gameData, creatureIds.getFirst());
            if (target != null) {
                StackEntry placementEntry = new StackEntry(entry);
                placementEntry.setControllerId(targetPlayerId);
                permanentCounterSupport.placeCounterOnPermanent(gameData, placementEntry, target,
                        counterEffect.counterType(), counterEffect.count());
            }
            return;
        }

        playerInputService.beginMultiPermanentChoice(gameData, targetPlayerId, creatureIds, 1,
                new MultiPermanentChoiceContext.OwnPermanentCounterPlacementByPlayer(
                        counterEffect.counterType(), counterEffect.count(), targetPlayerId),
                entry.getCard().getName() + " - Choose a creature to put a counter on.");
    }
}
