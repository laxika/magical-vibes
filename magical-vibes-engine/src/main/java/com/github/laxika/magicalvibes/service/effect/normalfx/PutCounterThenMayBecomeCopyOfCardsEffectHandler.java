package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfOneOfCardsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterThenMayBecomeCopyOfCardsEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PutCounterThenMayBecomeCopyOfCardsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PutCountersOnSourceEffectHandler putCountersOnSourceEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCounterThenMayBecomeCopyOfCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        List<Card> cards = ((PutCounterThenMayBecomeCopyOfCardsEffect) effect).triggeringCards().stream()
                .filter(card -> !card.isToken()
                        && (card.hasType(CardType.ARTIFACT) || card.hasType(CardType.CREATURE)))
                .toList();
        if (cards.isEmpty()) {
            return;
        }

        putCountersOnSourceEffectHandler.resolve(
                gameData, entry, new PutCountersOnSourceEffect(1, 1, 1));
        gameData.pendingMayAbilities.add(new PendingMayAbility(
                entry.getCard(),
                entry.getControllerId(),
                List.of(new BecomeCopyOfOneOfCardsUntilEndOfTurnEffect(cards)),
                "Have " + entry.getCard().getName()
                        + " become a copy of an artifact or creature card that left your graveyard?",
                null,
                null,
                entry.getSourcePermanentId()));
    }
}
