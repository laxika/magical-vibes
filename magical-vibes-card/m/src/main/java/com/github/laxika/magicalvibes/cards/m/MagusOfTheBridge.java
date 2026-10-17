package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "MH2", collectorNumber = "92")
public class MagusOfTheBridge extends Card {

    private static final CreateTokenEffect CREATE_ZOMBIE = CreateTokenEffect.blackZombie(1);

    public MagusOfTheBridge() {
        // Whenever a nontoken creature is put into your graveyard from the battlefield, create a
        // Zombie. The destination-based slot is keyed to the graveyard owner rather than the
        // creature's last controller, excludes tokens, and also sees this creature's own death.
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(new PermanentIsCreaturePredicate(), CREATE_ZOMBIE));

        // When a creature is put into an opponent's graveyard from the battlefield, exile this
        // creature.
        addEffect(EffectSlot.ON_PERMANENT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsCreaturePredicate(), new ExileSelfEffect()));
    }
}
