package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SpellManaSpentAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "134")
public class ThunderdrumSoloist extends Card {

    public ThunderdrumSoloist() {
        // Reach is loaded from Scryfall.

        // Opus — Whenever you cast an instant or sorcery spell, this creature deals 1 damage to each opponent.
        // If five or more mana was spent to cast that spell, this creature deals 3 damage to each opponent instead.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)
                )),
                List.of(new ConditionalReplacementEffect(
                        new SpellManaSpentAtLeast(5),
                        new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT),
                        new DealDamageToPlayersEffect(3, DamageRecipient.EACH_OPPONENT)
                ))
        ));
    }
}
