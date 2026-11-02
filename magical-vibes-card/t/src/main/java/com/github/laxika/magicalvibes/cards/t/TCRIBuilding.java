package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TMT", collectorNumber = "189")
public class TCRIBuilding extends Card {

    public TCRIBuilding() {
        // This land enters tapped.
        addStaticEffect(new EntersTappedEffect());

        // When this land enters, you gain 1 life.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(1));

        // {T}: Add {U} or {R}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.BLUE, ManaColor.RED))),
                "{T}: Add {U} or {R}."
        ));
    }
}
