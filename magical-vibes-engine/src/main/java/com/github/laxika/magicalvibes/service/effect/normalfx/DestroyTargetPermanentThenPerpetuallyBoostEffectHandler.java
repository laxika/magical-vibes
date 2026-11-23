package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenPerpetuallyBoostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Destroys a target permanent and gives the Dragon beheld for the spell a perpetual boost (Territorial Strike: "If a
 * Dragon creature was beheld, it perpetually gets +2/+2").
 */
@Component
@RequiredArgsConstructor
public class DestroyTargetPermanentThenPerpetuallyBoostEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetPermanentThenPerpetuallyBoostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var boost = (DestroyTargetPermanentThenPerpetuallyBoostEffect) effect;
        List<UUID> effectTargets = entry.targetsForEffect(effect);
        UUID targetId = effectTargets.isEmpty() ? entry.getTargetId() : effectTargets.getFirst();
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        var card = target.getCard();
        entry.rememberLastKnownPermanentCard(target.getId(), card);
        destructionSupport.tryDestroyAndLog(gameData, target, entry.getCard().getName(), false);
        boostBeheldDragon(gameData, entry, boost);
    }

    /**
     * "It" is the beheld Dragon: the permanent chosen or the card revealed from the hand (CR 701.4a), while it is still
     * where it was beheld (CR 400.7). A card in a hand is hidden, so the log names only the hand.
     */
    private void boostBeheldDragon(GameData gameData, StackEntry entry,
                                   DestroyTargetPermanentThenPerpetuallyBoostEffect boost) {
        String change = " perpetually gets +" + boost.powerBoost() + "/+" + boost.toughnessBoost() + ".";
        if (entry.getBeholdPermanentId() != null) {
            Permanent dragon = gameQueryService.findPermanentById(gameData, entry.getBeholdPermanentId());
            if (dragon != null) {
                PerpetualCardPowerToughnessSupport.remember(gameData, dragon.getCard(), boost.powerBoost(),
                        boost.toughnessBoost());
                gameLogService.append(gameData, dragon.isFaceDown()
                        ? GameLog.text(dragon.getPublicName() + change) : GameLog.cardThen(dragon.getCard(), change));
            }
            return;
        }
        UUID beheldCardId = entry.getBeholdCardId();
        if (beheldCardId == null) {
            return;
        }
        UUID controllerId = entry.getControllerId();
        for (Card card : gameData.playerHands.getOrDefault(controllerId, List.of())) {
            if (card.getId().equals(beheldCardId)) {
                PerpetualCardPowerToughnessSupport.remember(gameData, card, boost.powerBoost(), boost.toughnessBoost());
                gameLogService.append(gameData, GameLog.text(
                        "A card in " + gameData.playerIdToName.get(controllerId) + "'s hand" + change));
                return;
            }
        }
    }
}
