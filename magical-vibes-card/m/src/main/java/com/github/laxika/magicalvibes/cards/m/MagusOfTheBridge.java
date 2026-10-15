package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsToken;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "MH2", collectorNumber = "92")
public class MagusOfTheBridge extends Card {

    private static final CreateTokenEffect CREATE_ZOMBIE = CreateTokenEffect.blackZombie(1);

    public MagusOfTheBridge() {
        // Whenever a nontoken creature is put into your graveyard from the battlefield, create a
        // Zombie. The destination-based slot is keyed to the graveyard owner rather than the
        // creature's last controller and already excludes tokens.
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                new TriggeringCardConditionalEffect(new CardTypePredicate(CardType.CREATURE), CREATE_ZOMBIE));

        // The source's own death is not seen by the destination-based battlefield watcher, so its
        // self-death trigger is registered separately.
        addEffect(EffectSlot.ON_DEATH,
                new ConditionalEffect(new NotCondition(new SourceIsToken()), CREATE_ZOMBIE));

        // When a creature is put into an opponent's graveyard from the battlefield, exile this
        // creature.
        addEffect(EffectSlot.ON_PERMANENT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsCreaturePredicate(), new ExileSelfEffect()));
    }
}
