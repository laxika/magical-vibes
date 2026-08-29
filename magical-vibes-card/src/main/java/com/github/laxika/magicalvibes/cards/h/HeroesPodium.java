package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostLegendaryCreaturesByOtherLegendaryCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "BNG", collectorNumber = "159")
public class HeroesPodium extends Card {

    public HeroesPodium() {
        addEffect(EffectSlot.STATIC, new BoostLegendaryCreaturesByOtherLegendaryCreaturesEffect(1, 1));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}",
                List.of(new LookAtTopCardsEffect(
                        new XValue(), new Fixed(1), new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardSupertypePredicate(CardSupertype.LEGENDARY))),
                        LookDestination.BOTTOM_OF_LIBRARY_RANDOM, false,
                        LibrarySearchDestination.HAND, true)),
                "{X}, {T}: Look at the top X cards of your library. You may reveal a legendary creature card from among them and put it into your hand. Put the rest on the bottom of your library in a random order."
        ));
    }
}
