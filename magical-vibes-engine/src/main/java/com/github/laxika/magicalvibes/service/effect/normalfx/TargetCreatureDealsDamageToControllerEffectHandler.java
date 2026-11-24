package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsDamageToControllerEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link TargetCreatureDealsDamageToControllerEffect}: each dealer the subject names, as it is or as it last
 * existed (CR 608.2h), deals the damage through {@link DamageSupport#entryDealtByPermanent}, so it is the damage's
 * source (CR 120.1). The amount is evaluated once per dealer as the effect resolves; prevention, protection and the
 * damage replacements are applied by the damage event, never checked here first. The damage is this step's
 * {@code DEAL_DAMAGE} result.
 */
@Component
@RequiredArgsConstructor
public class TargetCreatureDealsDamageToControllerEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final AmountEvaluationService amountEvaluationService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetCreatureDealsDamageToControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetCreatureDealsDamageToControllerEffect) effect;
        entry.recordStep(StepKind.DEAL_DAMAGE, StepOutcome.NONE);
        List<UUID> sharedPlayers = e.playerIsEachDealers()
                ? List.of()
                : subjectResolutionService.players(gameData, entry, e, e.player());
        for (UUID dealerId : subjectResolutionService.permanentIds(gameData, entry, e, e.subject())) {
            Permanent dealer = damageSupport.dealerAsItIsOrWas(gameData, entry, dealerId);
            if (dealer == null) {
                continue;
            }
            UUID controllerId = damageSupport.dealerControllerId(gameData, entry, dealer);
            if (controllerId == null) {
                continue;
            }
            List<UUID> players = switch (e.player()) {
                case PlayerSubject.ControllerOf ignored -> List.of(controllerId);
                case PlayerSubject.OwnerOf ignored -> dealerOwner(gameData, entry, e, dealer);
                default -> sharedPlayers;
            };
            if (players.isEmpty()) {
                continue;
            }
            int damage = amountEvaluationService.evaluate(gameData, e.damage(),
                    AmountContext.forStackEntry(entry, dealer));
            StackEntry damageEntry = damageSupport.entryDealtByPermanent(gameData, entry, dealer, controllerId);
            for (UUID playerId : players) {
                damageSupport.dealDamageToPlayer(gameData, damageEntry, playerId, damage);
            }
            entry.addToStep(StepKind.DEAL_DAMAGE, damageEntry.getResolutionResults().outcome(StepKind.DEAL_DAMAGE));
        }
    }

    /**
     * "Its owner": the owner of the dealer's card, as it is or as it last existed (a card's owner never changes,
     * CR 108.3).
     */
    private List<UUID> dealerOwner(GameData gameData, StackEntry entry, TargetCreatureDealsDamageToControllerEffect e,
                                   Permanent dealer) {
        Card card = dealer.getOriginalCard() != null ? dealer.getOriginalCard() : dealer.getCard();
        UUID owner = gameData.ownerOf(card);
        return owner != null ? List.of(owner) : subjectResolutionService.players(gameData, entry, e,
                Subjects.ownerOf(Subjects.named(dealer.getId())));
    }
}
