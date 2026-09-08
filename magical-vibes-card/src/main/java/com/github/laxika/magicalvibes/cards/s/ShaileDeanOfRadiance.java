package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.e.EmbroseDeanOfShadow;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentEnteredBattlefieldThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "158")
public class ShaileDeanOfRadiance extends Card {

    public ShaileDeanOfRadiance() {
        EmbroseDeanOfShadow backFace = new EmbroseDeanOfShadow();
        setBackFaceCard(backFace);
        setModalDoubleFaced(true);

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PutCounterOnEachControlledPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE,
                        1,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentEnteredBattlefieldThisTurnPredicate())))),
                "{T}: Put a +1/+1 counter on each creature that entered the battlefield under your control this turn."
        ));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Shaile, Dean of Radiance", List.of()),
                new ChooseOneEffect.ChooseOneOption(
                        "Embrose, Dean of Shadow", backFace.getEffects(EffectSlot.SPELL)))));
    }

    @Override
    public String getBackFaceClassName() {
        return "EmbroseDeanOfShadow";
    }
}
