package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BolsterEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the keyword action "bolster N". */
@Component
@RequiredArgsConstructor
public class BolsterEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BolsterEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        BolsterEffect bolster = (BolsterEffect) effect;
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int amount = amountEvaluationService.evaluate(gameData, bolster.amount(),
                AmountContext.forStackEntry(entry, source));
        if (amount <= 0) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }

        List<Permanent> creatures = battlefield.stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .toList();
        if (creatures.isEmpty()) {
            return;
        }

        int leastToughness = creatures.stream()
                .mapToInt(permanent -> gameQueryService.getEffectiveToughness(gameData, permanent))
                .min()
                .orElseThrow();
        List<Permanent> leastToughnessCreatures = creatures.stream()
                .filter(permanent -> gameQueryService.getEffectiveToughness(gameData, permanent) == leastToughness)
                .toList();

        if (leastToughnessCreatures.size() == 1) {
            permanentCounterSupport.placeCounterOnPermanent(gameData, entry,
                    leastToughnessCreatures.getFirst(), CounterType.PLUS_ONE_PLUS_ONE, amount);
            return;
        }

        List<UUID> eligibleIds = leastToughnessCreatures.stream().map(Permanent::getId).toList();
        playerInputService.beginMultiPermanentChoice(gameData, controllerId, eligibleIds, 1,
                new MultiPermanentChoiceContext.OwnPermanentCounterPlacement(
                        CounterType.PLUS_ONE_PLUS_ONE, amount),
                "Choose a creature to bolster.");
    }
}
