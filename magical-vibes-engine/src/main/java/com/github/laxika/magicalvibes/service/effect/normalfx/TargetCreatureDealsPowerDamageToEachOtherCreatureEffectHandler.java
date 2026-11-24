package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsPowerDamageToEachOtherCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link TargetCreatureDealsPowerDamageToEachOtherCreatureEffect}: the dealer, as it is (a target must still
 * be on the battlefield) or, for a dealer that is not a target, as it last existed (CR 608.2h), deals damage equal to
 * its power, fixed once, to each creature the recipients name but itself, through
 * {@link DamageSupport#entryDealtByPermanent}. Protection from the dealer prevents a recipient's damage (CR 702.16e);
 * every other prevention is applied by the damage event. The damage is this step's {@code DEAL_DAMAGE} result.
 */
@Component
@RequiredArgsConstructor
public class TargetCreatureDealsPowerDamageToEachOtherCreatureEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetCreatureDealsPowerDamageToEachOtherCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetCreatureDealsPowerDamageToEachOtherCreatureEffect) effect;
        entry.recordStep(StepKind.DEAL_DAMAGE, StepOutcome.NONE);
        List<UUID> dealerIds = subjectResolutionService.permanentIds(gameData, entry, e, e.dealer());
        Permanent dealer = dealerIds.isEmpty() ? null
                : e.dealer().declaresTarget()
                ? gameQueryService.findPermanentById(gameData, dealerIds.getFirst())
                : damageSupport.dealerAsItIsOrWas(gameData, entry, dealerIds.getFirst());
        if (dealer == null) {
            return;
        }
        UUID controllerId = damageSupport.dealerControllerId(gameData, entry, dealer);
        if (controllerId == null) {
            return;
        }
        List<Permanent> recipients = subjectResolutionService.permanents(gameData, entry, e, e.recipients()).stream()
                .filter(permanent -> !permanent.getId().equals(dealer.getId()))
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .toList();
        int power = amountEvaluationService.evaluate(gameData, new SourcePower(),
                AmountContext.forStackEntry(entry, dealer));
        if (power <= 0) {
            return; // A source that would deal 0 damage deals none (CR 120.8).
        }
        StackEntry damageEntry = damageSupport.entryDealtByPermanent(gameData, entry, dealer, controllerId);
        for (Permanent recipient : recipients) {
            if (gameQueryService.isDamagePreventable(gameData)
                    && !gameQueryService.damageCantBePreventedFromSource(gameData, dealer)
                    && gameQueryService.hasProtectionFromSource(gameData, recipient, dealer)) {
                gameLogService.append(gameData, GameLog.cardTextCard(recipient.getCard(),
                        " has protection — damage from ", dealer.getCard(), " prevented."));
                continue;
            }
            damageSupport.dealCreatureDamage(gameData, damageEntry, recipient, power, dealer);
        }
        entry.addToStep(StepKind.DEAL_DAMAGE, damageEntry.getResolutionResults().outcome(StepKind.DEAL_DAMAGE));
    }
}
