package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachOtherOpponentEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves combat-damage triggers that damage every other opponent separately. */
@Component
@RequiredArgsConstructor
public class DealDamageToEachOtherOpponentEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToEachOtherOpponentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToEachOtherOpponentEffect) effect;
        UUID controllerId = entry.getControllerId();
        if (controllerId == null) {
            return;
        }

        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int amount = Math.max(0, amountEvaluationService.evaluate(
                gameData, e.amount(), AmountContext.forStackEntry(entry, source)));
        if (amount == 0) {
            return;
        }

        UUID damagedOpponentId = entry.getTargetId();
        StackEntry damageEntry = entry;
        if (e.dealerIsSource() && entry.getTriggeringPermanentId() != null) {
            // "it deals that much damage": the creature that dealt the combat damage is the damage source.
            Permanent dealer = gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
            Card dealerCard = dealer == null ? entry.getDamageSourceCard() : dealer.getCard();
            UUID dealerControllerId = dealer == null
                    ? entry.getTriggeringPermanentControllerId()
                    : gameQueryService.findPermanentController(gameData, dealer.getId());
            if (dealerCard == null || dealerControllerId == null) {
                return;
            }
            damageEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    dealerCard,
                    dealerControllerId,
                    dealerCard.getName() + "'s ability",
                    List.of(),
                    damagedOpponentId,
                    dealer == null ? null : dealer.getId());
            damageEntry.setDamageSourceCard(dealerCard);
        }
        if (damageSupport.isDamageSourcePreventedWithLog(gameData, damageEntry)) {
            return;
        }

        for (UUID opponentId : gameData.orderedPlayerIds) {
            if (opponentId.equals(controllerId) || opponentId.equals(damagedOpponentId)) {
                continue;
            }
            int damage = amount;
            damageSupport.dealDamageToPlayer(gameData, damageEntry, opponentId, damage);
        }
        gameOutcomeService.checkWinCondition(gameData);
    }
}
