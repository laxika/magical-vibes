package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessBlightsEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a ward ransom that requires the spell's controller to blight a creature. */
@Component
@RequiredArgsConstructor
public class CounterUnlessBlightsEffectHandler implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterUnlessBlightsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CounterUnlessBlightsEffect blightEffect = (CounterUnlessBlightsEffect) effect;
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        StackEntry targetEntry = counterSupport.findCounterTarget(gameData, targetCardId, entry);
        if (targetEntry == null) return;

        UUID targetControllerId = targetEntry.getControllerId();
        if (controlledCreatureIds(gameData, targetControllerId).isEmpty()) {
            counterSupport.counterSpell(gameData, entry, targetEntry);
            return;
        }

        Card targetCard = targetEntry.getCard();
        String prompt = "Blight " + blightEffect.count() + " to prevent "
                + targetCard.getName() + " from being countered?";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), targetControllerId, List.of(blightEffect), prompt,
                targetCardId, entry.getControllerId()));
    }

    private List<UUID> controlledCreatureIds(GameData gameData, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return List.of();

        return battlefield.stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .filter(permanent -> !gameQueryService.cantHaveCounters(gameData, permanent))
                .filter(permanent -> !gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, permanent))
                .map(Permanent::getId)
                .toList();
    }
}
