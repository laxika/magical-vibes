package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "263")
public class ArchwayCommons extends Card {

    public ArchwayCommons() {
        // This land enters tapped.
        addStaticEffect(new EntersTappedEffect());

        // When this land enters, sacrifice it unless you pay {1}.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ForcedCostOrElseEffect(
                        new PayManaCost("{1}"),
                        List.of(new SacrificeSelfEffect()),
                        true));

        // {T}: Add one mana of any color.
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
