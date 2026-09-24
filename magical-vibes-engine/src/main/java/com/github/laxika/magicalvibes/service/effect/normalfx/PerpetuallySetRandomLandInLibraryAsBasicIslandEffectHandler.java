package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallySetRandomLandInLibraryAsBasicIslandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class PerpetuallySetRandomLandInLibraryAsBasicIslandEffectHandler implements NormalEffectHandlerBean {

    private static final String GAINED_ABILITY = "When this land enters, draw a card.";

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallySetRandomLandInLibraryAsBasicIslandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Card> library = gameData.playerDecks.get(entry.getControllerId());
        if (library == null || library.isEmpty()) {
            return;
        }

        List<Card> landCards = library.stream().filter(card -> card.hasType(com.github.laxika.magicalvibes.model.CardType.LAND)).toList();
        if (landCards.isEmpty()) {
            return;
        }

        Card selected = landCards.get(ThreadLocalRandom.current().nextInt(landCards.size()));
        Card modified = selected.createRuntimeCopy();

        EnumSet<CardSupertype> supertypes = modified.getSupertypes().isEmpty()
                ? EnumSet.noneOf(CardSupertype.class)
                : EnumSet.copyOf(modified.getSupertypes());
        supertypes.add(CardSupertype.BASIC);
        modified.setSupertypes(supertypes);

        List<CardSubtype> subtypes = new ArrayList<>(modified.getSubtypes());
        if (!subtypes.contains(CardSubtype.ISLAND)) {
            subtypes.add(CardSubtype.ISLAND);
        }
        modified.setSubtypes(subtypes);
        modified.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect());
        String cardText = modified.getCardText();
        modified.setCardText((cardText == null || cardText.isBlank() ? "" : cardText + "\n") + GAINED_ABILITY);
        modified.freeze();

        int selectedIndex = library.indexOf(selected);
        library.set(selectedIndex, modified);
        gameLogService.append(gameData, GameLog.cardThen(selected,
                " perpetually becomes a basic Island in addition to its other types and gains \""
                        + GAINED_ABILITY + "\"."));
    }
}
