package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "215")
public class TurntimberSymbiosis extends Card {

    public TurntimberSymbiosis() {
        setBackFaceCard(new TurntimberSerpentineWood());
        setModalDoubleFaced(true);

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Turntimber Symbiosis", List.of(
                        new LookAtTopCardsEffect(
                                new Fixed(7), new Fixed(1), new CardTypePredicate(CardType.CREATURE),
                                LookDestination.BOTTOM_OF_LIBRARY_RANDOM, false,
                                LibrarySearchDestination.BATTLEFIELD, true, false, null,
                                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(3),
                                        new CardMaxManaValuePredicate(3))))),
                new ChooseOneEffect.ChooseOneOption("Turntimber, Serpentine Wood", List.of())
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "TurntimberSerpentineWood";
    }
}
