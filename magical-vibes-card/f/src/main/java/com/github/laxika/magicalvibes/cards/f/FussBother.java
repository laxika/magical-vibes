package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

/** Fuss // Bother, a split spell with one mode for each half. */
@CardRegistration(set = "MKM", collectorNumber = "248")
public class FussBother extends Card {

    public FussBother() {
        CardEffect fuss = new PutCounterOnEachControlledPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE,
                1,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsAttackingPredicate())));
        CardEffect bother = new CreateTokenEffect(
                3,
                "Thopter",
                1,
                1,
                null,
                List.of(CardSubtype.THOPTER),
                Set.of(Keyword.FLYING),
                Set.of(CardType.ARTIFACT));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Fuss — Put a +1/+1 counter on each attacking creature you control",
                        fuss
                ).withManaCost("{2}{R/W}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Bother — Create three 1/1 colorless Thopter artifact creature tokens with flying. Surveil 2",
                        List.of(bother, new SurveilEffect(2))
                ).withManaCost("{4}{W/U}{W/U}")
        )));
    }
}
