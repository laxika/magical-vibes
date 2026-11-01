package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH2", collectorNumber = "92")
public class MagusOfTheBridge extends Card {

    private static final CreateTokenEffect CREATE_ZOMBIE = CreateTokenEffect.blackZombie(1);

    public MagusOfTheBridge() {
        // Whenever a nontoken creature is put into your graveyard from the battlefield, create a
        // Zombie. The destination-based slot is keyed to the graveyard owner rather than the
        // creature's last controller, excludes tokens, and also sees this creature's own death.
        addTrigger(Triggers.dies(Triggers.NONTOKEN, ObjectRelation.YOU_OWN),
                new TriggeringPermanentConditionalEffect(new PermanentIsCreaturePredicate(), CREATE_ZOMBIE));

        // When a creature is put into an opponent's graveyard from the battlefield, exile this
        // creature.
        addTrigger(Triggers.dies(null, ObjectRelation.OPPONENT_OWNS),
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsCreaturePredicate(), new ExileSelfEffect()));
    }
}
