package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PerpetualPowerToughnessModifier;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostOtherControlledCreaturesAndHandCardsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PerpetuallyBoostOtherControlledCreaturesAndHandCardsEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyBoostOtherControlledCreaturesAndHandCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var boost = (PerpetuallyBoostOtherControlledCreaturesAndHandCardsEffect) effect;
        UUID controllerId = entry.getControllerId();
        UUID sourcePermanentId = entry.getSourcePermanentId();
        int affected = 0;

        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand != null) {
            for (Card card : hand) {
                if (card.hasType(com.github.laxika.magicalvibes.model.CardType.CREATURE)) {
                    mergeHandModifier(gameData, card, boost);
                    affected++;
                }
            }
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (permanent.getId().equals(sourcePermanentId)
                        || !permanent.getCard().hasType(com.github.laxika.magicalvibes.model.CardType.CREATURE)) {
                    continue;
                }
                mergeBattlefieldModifier(gameData, permanent.getCard(), boost);
                affected++;
            }
        }

        if (affected > 0) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    " perpetually gives other creatures you control and creature cards in your hand "
                            + formatModifier(boost.powerBoost(), boost.toughnessBoost()) + "."));
        }
    }

    private void mergeHandModifier(GameData gameData, Card card,
                                   PerpetuallyBoostOtherControlledCreaturesAndHandCardsEffect effect) {
        gameData.perpetualPowerToughnessModifiers.merge(
                card.getId(),
                new PerpetualPowerToughnessModifier(effect.powerBoost(), effect.toughnessBoost()),
                PerpetualPowerToughnessModifier::add);
    }

    private void mergeBattlefieldModifier(GameData gameData, Card card,
                                          PerpetuallyBoostOtherControlledCreaturesAndHandCardsEffect effect) {
        gameData.perpetualCardPowerToughnessModifiers.merge(
                card.getId(),
                new GameData.PerpetualPowerToughnessModifier(effect.powerBoost(), effect.toughnessBoost()),
                (oldValue, newValue) -> new GameData.PerpetualPowerToughnessModifier(
                        oldValue.power() + newValue.power(), oldValue.toughness() + newValue.toughness()));
    }

    private static String formatModifier(int power, int toughness) {
        return String.format("%+d/%+d", power, toughness);
    }
}
