package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostOtherOwnCreaturesAndHandCreatureCardsOfChosenSubtypeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PerpetuallyBoostOtherOwnCreaturesAndHandCreatureCardsOfChosenSubtypeEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyBoostOtherOwnCreaturesAndHandCreatureCardsOfChosenSubtypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var boost = (PerpetuallyBoostOtherOwnCreaturesAndHandCreatureCardsOfChosenSubtypeEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || source.getChosenSubtype() == null) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        CardSubtype chosenSubtype = source.getChosenSubtype();
        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(controllerId, List.of());
        for (Permanent permanent : battlefield) {
            if (permanent.getId().equals(entry.getSourcePermanentId())
                    || !gameQueryService.isCreature(gameData, permanent)
                    || !gameQueryService.hasEffectiveSubtype(gameData, permanent, chosenSubtype)) {
                continue;
            }
            PerpetualCardPowerToughnessSupport.remember(
                    gameData, permanent.getCard(), boost.powerBoost(), boost.toughnessBoost());
            PerpetualCardPowerToughnessSupport.applyToPermanent(
                    gameData, controllerId, permanent, boost.powerBoost(), boost.toughnessBoost());
        }

        for (Card card : gameData.playerHands.getOrDefault(controllerId, List.of())) {
            if (gameQueryService.cardHasType(card, CardType.CREATURE, gameData, controllerId)
                    && gameQueryService.cardHasSubtype(card, chosenSubtype, gameData, controllerId)) {
                PerpetualCardPowerToughnessSupport.remember(
                        gameData, card, boost.powerBoost(), boost.toughnessBoost());
            }
        }
    }
}
