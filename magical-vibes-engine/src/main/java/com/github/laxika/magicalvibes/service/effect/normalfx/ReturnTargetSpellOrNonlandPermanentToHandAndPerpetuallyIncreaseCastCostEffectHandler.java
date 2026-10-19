package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetSpellOrNonlandPermanentToHandAndPerpetuallyIncreaseCastCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.cast.PerpetualCardCastCostSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves a mixed-zone bounce of a spell or nonland permanent, with its perpetual generic cost increase
 * applied only when the effect's perpetual condition is met (Talion's Throneguard, when bargained).
 */
@Component
@RequiredArgsConstructor
public class ReturnTargetSpellOrNonlandPermanentToHandAndPerpetuallyIncreaseCastCostEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final BounceSupport bounceSupport;
    private final ConditionEvaluationService conditionEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetSpellOrNonlandPermanentToHandAndPerpetuallyIncreaseCastCostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ReturnTargetSpellOrNonlandPermanentToHandAndPerpetuallyIncreaseCastCostEffect bounceEffect =
                (ReturnTargetSpellOrNonlandPermanentToHandAndPerpetuallyIncreaseCastCostEffect) effect;
        List<UUID> targetIds = entry.targetsForEffect(bounceEffect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }

        boolean taxApplies = bounceEffect.perpetualCondition() == null
                || conditionEvaluationService.isMet(gameData, bounceEffect.perpetualCondition(),
                ConditionContext.forStackEntry(entry));
        boolean returnedPermanent = false;
        for (UUID targetId : targetIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, targetId);
            if (permanent != null) {
                Card card = permanent.getCard();
                if (permanentRemovalService.removePermanentToHand(gameData, permanent)) {
                    if (taxApplies && !card.isToken()) {
                        PerpetualCardCastCostSupport.rememberIncrease(gameData, card, bounceEffect.amount());
                        gameLogService.append(gameData, GameLog.cardThen(card,
                                " is returned to its owner's hand and perpetually costs "
                                        + bounceEffect.amount() + " generic mana more to cast."));
                    } else {
                        gameLogService.append(gameData, GameLog.cardThen(card,
                                " is returned to its owner's hand."));
                    }
                    returnedPermanent = true;
                }
                continue;
            }

            StackEntry spell = findSpell(gameData, targetId);
            if (spell == null || spell.isCopy()) {
                continue;
            }
            Card card = spell.getPhysicalCard();
            bounceSupport.returnSpellToOwnerHand(gameData, entry, targetId);
            if (taxApplies && card != null) {
                PerpetualCardCastCostSupport.rememberIncrease(gameData, card, bounceEffect.amount());
            }
        }

        if (returnedPermanent) {
            permanentRemovalService.removeOrphanedAuras(gameData);
        }
    }

    private StackEntry findSpell(GameData gameData, UUID targetId) {
        return gameData.stack.stream()
                .filter(stackEntry -> stackEntry.getTargetableId().equals(targetId))
                .filter(this::isSpell)
                .findFirst()
                .orElse(null);
    }

    private boolean isSpell(StackEntry stackEntry) {
        return switch (stackEntry.getEntryType()) {
            case INSTANT_SPELL, SORCERY_SPELL, CREATURE_SPELL, ENCHANTMENT_SPELL,
                    ARTIFACT_SPELL, PLANESWALKER_SPELL, BATTLE_SPELL -> true;
            default -> false;
        };
    }
}
