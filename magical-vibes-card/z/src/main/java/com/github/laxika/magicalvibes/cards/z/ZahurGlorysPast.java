package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.MaxSpeed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DFT", collectorNumber = "229")
public class ZahurGlorysPast extends Card {

    private static final CreateTokenEffect TAPPED_ZOMBIE = new CreateTokenEffect(
            1, "Zombie", 2, 2, CardColor.BLACK, List.of(CardSubtype.ZOMBIE), Set.of(), Set.of(), true);
    private static final CardEffect MAX_SPEED_ZOMBIE = TAPPED_ZOMBIE;

    public ZahurGlorysPast() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentCost(new PermanentIsCreaturePredicate(), "Sacrifice another creature"),
                        new SurveilEffect(1)),
                "Sacrifice another creature: Surveil 1. Activate only once each turn.",
                1
        ));

        addTrigger(Triggers.nontokenCreatureDies(ObjectRelation.YOU_CONTROL),
                TriggerOptions.DEFAULT.withInterveningIf(new MaxSpeed()),
                MAX_SPEED_ZOMBIE);
    }
}
