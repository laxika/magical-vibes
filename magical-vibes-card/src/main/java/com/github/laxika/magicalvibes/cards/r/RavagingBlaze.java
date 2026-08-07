package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "159")
public class RavagingBlaze extends Card {

    public RavagingBlaze() {
        // Ravaging Blaze deals X damage to target creature.
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(new XValue()));

        // Spell mastery — If there are two or more instant and/or sorcery cards in your graveyard,
        // Ravaging Blaze also deals X damage to that creature's controller.
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new GraveyardCardThreshold(2, new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)
        ))), new DealDamageToPlayersEffect(new XValue(), DamageRecipient.TARGET_PERMANENT_CONTROLLER)));
    }
}
