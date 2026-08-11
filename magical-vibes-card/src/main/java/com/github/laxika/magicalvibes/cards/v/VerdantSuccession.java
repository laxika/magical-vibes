package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DyingCreatureControllerMaySearchLibraryForSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "280")
public class VerdantSuccession extends Card {

    public VerdantSuccession() {
        // Whenever a green nontoken creature dies, that creature's controller may search their
        // library for a card with the same name and put it onto the battlefield, then shuffle.
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new TriggeringCardConditionalEffect(
                new CardAllOfPredicate(List.of(
                        new CardColorPredicate(CardColor.GREEN),
                        new CardNotPredicate(new CardIsTokenPredicate()))),
                new DyingCreatureControllerMaySearchLibraryForSameNameEffect()));
    }
}
