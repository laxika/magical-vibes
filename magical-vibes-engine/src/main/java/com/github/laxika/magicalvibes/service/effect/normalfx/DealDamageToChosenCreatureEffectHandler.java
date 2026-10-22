package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToChosenCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link DealDamageToChosenCreatureEffect}: the controller chooses a matching creature as the
 * effect resolves, then it is dealt damage like any other creature damage from the resolving source.
 */
@Component
@RequiredArgsConstructor
public class DealDamageToChosenCreatureEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToChosenCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToChosenCreatureEffect) effect;

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int damage = amountEvaluationService.evaluate(gameData, e.damage(),
                AmountContext.forStackEntry(entry, source));

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() == null ? null : entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentId(entry.getSourcePermanentId())
                .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());
        List<UUID> eligibleIds = new ArrayList<>();
        gameData.forEachPermanent((ownerId, permanent) -> {
            if (gameQueryService.isCreature(gameData, permanent)
                    && (e.filter() == null
                    || predicateEvaluationService.matchesPermanentPredicate(permanent, e.filter(), filterContext))) {
                eligibleIds.add(permanent.getId());
            }
        });
        if (eligibleIds.isEmpty()) {
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.DealDamageToChosenCreature(entry, damage));
        playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), eligibleIds,
                "Choose a creature to be dealt " + damage + " damage.");
    }

    public void completeChoice(GameData gameData, UUID permanentId,
                               PermanentChoiceContext.DealDamageToChosenCreature context) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            return;
        }
        StackEntry entry = context.resolvingEntry();
        if (damageSupport.isDamagePreventedForCreature(gameData, entry, target)) {
            return;
        }
        damageSupport.dealCreatureDamage(gameData, entry, target, context.damage());
    }
}
