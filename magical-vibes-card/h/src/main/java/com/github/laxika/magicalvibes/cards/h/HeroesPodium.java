package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "BNG", collectorNumber = "159")
@CardRegistration(set = "WHO", collectorNumber = "242")
@CardRegistration(set = "WHO", collectorNumber = "479")
@CardRegistration(set = "WHO", collectorNumber = "833")
@CardRegistration(set = "WHO", collectorNumber = "1070")
@CardRegistration(set = "DMC", collectorNumber = "185")
public class HeroesPodium extends Card {

    /** "For each other legendary creature you control", counted for each boosted creature. */
    private static final PermanentCount OTHER_LEGENDARY_CREATURES_YOU_CONTROL = new PermanentCount(
            new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                    new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY))),
            CountScope.CONTROLLER, true);

    public HeroesPodium() {
        addStaticEffect(new DynamicStaticBoostEffect(
                OTHER_LEGENDARY_CREATURES_YOU_CONTROL, OTHER_LEGENDARY_CREATURES_YOU_CONTROL,
                Subjects.creaturesYouControl(), new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY), true));

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
