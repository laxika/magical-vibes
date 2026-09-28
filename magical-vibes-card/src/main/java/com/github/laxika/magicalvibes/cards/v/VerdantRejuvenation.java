package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongControlled;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "69")
public class VerdantRejuvenation extends Card {

    public VerdantRejuvenation() {
        addEffect(EffectSlot.SPELL, new SeekLibraryEffect(
                new GreatestManaValueAmongControlled(new PermanentIsCreaturePredicate()),
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardTypePredicate(CardType.ENCHANTMENT),
                        new CardTypePredicate(CardType.PLANESWALKER))),
                LibrarySearchDestination.BATTLEFIELD));
    }
}
