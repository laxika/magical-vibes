package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.e.EmbroseDeanOfShadow;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
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
                List.of(new PutCounterOnEachMatchingPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE,
                        1,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentEnteredBattlefieldThisTurnPredicate(true))),
                        EachPermanentScope.ALL_PLAYERS)),
                "{T}: Put a +1/+1 counter on each creature that entered the battlefield under your control this turn."
        ));

        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Shaile, Dean of Radiance", List.of()),
                new ChooseOneEffect.ChooseOneOption(
                        "Embrose, Dean of Shadow", backFace.getSpellEffects()))));
    }

    @Override
    public String getBackFaceClassName() {
        return "EmbroseDeanOfShadow";
    }
}
