package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantTapDrawToRandomLandInLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class PerpetuallyGrantTapDrawToRandomLandInLibraryEffectHandler implements NormalEffectHandlerBean {

    private static final String GAINED_ABILITY = "Whenever this land becomes tapped, draw a card.";

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantTapDrawToRandomLandInLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Card> library = gameData.playerDecks.get(entry.getControllerId());
        if (library == null || library.isEmpty()) {
            return;
        }

        List<Card> landCards = library.stream().filter(card -> card.hasType(CardType.LAND)).toList();
        if (landCards.isEmpty()) {
            return;
        }

        Card selected = landCards.get(ThreadLocalRandom.current().nextInt(landCards.size()));
        Card modified = selected.createRuntimeCopy();
        modified.addEffect(EffectSlot.ON_ALLY_PERMANENT_BECOMES_TAPPED,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsSourceCardPredicate(), new DrawCardEffect()));
        String cardText = modified.getCardText();
        modified.setCardText((cardText == null || cardText.isBlank() ? "" : cardText + "\n") + GAINED_ABILITY);
        modified.freeze();

        library.set(library.indexOf(selected), modified);
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " perpetually gives a random land in its controller's library the ability \""
                        + GAINED_ABILITY + "\"."));
    }
}
