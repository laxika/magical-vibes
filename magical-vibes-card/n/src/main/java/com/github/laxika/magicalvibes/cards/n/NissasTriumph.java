package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "170")
public class NissasTriumph extends Card {

    public NissasTriumph() {
        // Search your library for up to two basic Forest cards. If you control a Nissa planeswalker,
        // instead search your library for up to three land cards. Reveal those cards, put them into
        // your hand, then shuffle.
        CardAllOfPredicate basicForest = new CardAllOfPredicate(List.of(
                new CardSupertypePredicate(CardSupertype.BASIC),
                new CardSubtypePredicate(CardSubtype.FOREST)));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.NISSA)),
                new SearchLibraryEffect(new Fixed(2), basicForest, LibrarySearchDestination.HAND),
                new SearchLibraryEffect(new Fixed(3), new CardTypePredicate(CardType.LAND), LibrarySearchDestination.HAND)
        ));
    }
}
