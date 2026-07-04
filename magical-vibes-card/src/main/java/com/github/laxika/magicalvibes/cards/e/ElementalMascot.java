package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SpellManaSpentAtLeast;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "185")
public class ElementalMascot extends Card {

    public ElementalMascot() {
        // Flying, vigilance are loaded from Scryfall.

        // Opus — Whenever you cast an instant or sorcery spell, this creature gets +1/+0 until end of turn.
        // If five or more mana was spent to cast that spell, exile the top card of your library.
        // You may play that card until the end of your next turn.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)
                )),
                List.of(
                        new BoostSelfEffect(1, 0),
                        new ConditionalEffect(new SpellManaSpentAtLeast(5),
                                new ExileTopCardsMayPlayUntilNextTurnEffect(1))
                )
        ));
    }
}
