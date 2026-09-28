package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsPowerDamageToTargetCreatureThenApplyPerpetualPowerToughnessEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TargetCreatureDealsPowerDamageToTargetCreatureThenApplyPerpetualPowerToughnessEffectHandler
        implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetCreatureDealsPowerDamageToTargetCreatureThenApplyPerpetualPowerToughnessEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetCreatureDealsPowerDamageToTargetCreatureThenApplyPerpetualPowerToughnessEffect) effect;
        int excessDamage = 0;

        List<UUID> sourceGroup = entry.targetsForGroup(e.sourceTargetGroup());
        List<UUID> victimGroup = entry.targetsForGroup(e.victimTargetGroup());
        if (!sourceGroup.isEmpty() && !victimGroup.isEmpty()) {
            Permanent source = gameQueryService.findPermanentById(gameData, sourceGroup.getFirst());
            Permanent victim = gameQueryService.findPermanentById(gameData, victimGroup.getFirst());
            if (source != null && victim != null
                    && gameQueryService.isCreature(gameData, source)
                    && gameQueryService.isCreature(gameData, victim)) {
                int markedDamageBefore = victim.getMarkedDamage();
                boolean deathtouch = gameQueryService.sourceHasKeyword(
                        gameData, entry, source, Keyword.DEATHTOUCH);

                if (!(gameQueryService.isDamagePreventable(gameData)
                        && gameQueryService.isPreventedFromDealingDamage(gameData, source))
                        && !(gameQueryService.isDamagePreventable(gameData)
                        && gameQueryService.hasProtectionFromSource(gameData, victim, source))) {
                    int power = gameQueryService.getPowerBasedDamage(gameData, source);
                    int rawDamage = gameQueryService.applyDamageMultiplier(gameData, power, entry);
                    int damageDealt = damageSupport.dealCreatureDamage(
                            gameData, entry, victim, rawDamage, source);
                    excessDamage = damageSupport.computeExcessDamageToCreature(
                            gameData, victim, damageDealt, markedDamageBefore, deathtouch);
                } else {
                    gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s damage is prevented."));
                }
            }
        }

        List<Card> hand = gameData.playerHands.getOrDefault(entry.getControllerId(), List.of());
        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            if (predicateEvaluationService.matchesCardPredicate(
                    hand.get(i), e.handCardFilter(), null, gameData, entry.getControllerId())) {
                validIndices.add(i);
            }
        }
        if (validIndices.isEmpty()) {
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.PerpetualPowerToughnessChoice(
                entry.getControllerId(), validIndices,
                "Choose a creature card in your hand. It perpetually gets +" + excessDamage
                        + "/+" + excessDamage + ".",
                excessDamage, excessDamage));
    }
}
