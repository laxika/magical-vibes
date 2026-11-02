package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "17")
public class KnightCaptainOfEos extends Card {

    public KnightCaptainOfEos() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), CreateTokenEffect.whiteSoldier(2));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(new SacrificePermanentCost(
                        new PermanentHasSubtypePredicate(CardSubtype.SOLDIER),
                        "Sacrifice a Soldier",
                        false
                ), PreventDamageEffect.allCombat()),
                "{W}, Sacrifice a Soldier: Prevent all combat damage that would be dealt this turn."
        ));
    }
}
