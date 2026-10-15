package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "211")
public class ForgingTheTyriteSword extends Card {

    public ForgingTheTyriteSword() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, CreateTokenEffect.ofTreasureToken(1));
        addEffect(EffectSlot.SAGA_CHAPTER_II, CreateTokenEffect.ofTreasureToken(1));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new SearchLibraryEffect(new CardAnyOfPredicate(List.of(
                new CardNamedPredicate("Halvar, God of Battle"),
                new CardSubtypePredicate(CardSubtype.EQUIPMENT)
        )), LibrarySearchDestination.HAND));
    }
}
