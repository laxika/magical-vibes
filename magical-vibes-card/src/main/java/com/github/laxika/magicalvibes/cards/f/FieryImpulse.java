package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "145")
public class FieryImpulse extends Card {

    public FieryImpulse() {
        // Fiery Impulse deals 2 damage to target creature.
        // Spell mastery — If there are two or more instant and/or sorcery cards in your graveyard,
        // Fiery Impulse deals 3 damage instead.
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new GraveyardCardThreshold(2, new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)
                ))),
                new DealDamageToTargetCreatureEffect(2),
                new DealDamageToTargetCreatureEffect(3)
        ));
    }
}
