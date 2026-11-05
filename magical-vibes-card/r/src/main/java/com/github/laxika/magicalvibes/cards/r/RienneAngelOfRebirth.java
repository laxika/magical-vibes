package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsMulticoloredPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M20", collectorNumber = "281")
@CardRegistration(set = "DMC", collectorNumber = "166")
public class RienneAngelOfRebirth extends Card {

    public RienneAngelOfRebirth() {
        addStaticEffect(new StaticBoostEffect(1, 0, GrantScope.OWN_CREATURES,
                new PermanentIsMulticoloredPredicate()));

        addTrigger(Triggers.creatureDies(new PermanentIsMulticoloredPredicate(), ObjectRelation.ANOTHER_YOU_CONTROL),
                new RegisterDelayedReturnCardFromGraveyardToHandEffect());
    }
}
