package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OTJ", collectorNumber = "102")
public class RictusRobber extends Card {

    public RictusRobber() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(new Morbid()),
                new CreateTokenEffect("Zombie Rogue", 2, 2, CardColor.BLUE,
                        Set.of(CardColor.BLUE, CardColor.BLACK),
                        List.of(CardSubtype.ZOMBIE, CardSubtype.ROGUE)));

        // Plot {2}{B}
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{2}{B}"))));
    }
}
