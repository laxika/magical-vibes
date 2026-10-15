package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.SourceCardInCommandZone;
import com.github.laxika.magicalvibes.model.condition.SourceCardOnBattlefield;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

/**
 * Eminence (CR 207.2c) is an ability word: the spell-cast trigger below functions while this card is
 * in its controller's command zone, and the same ability works while it is a battlefield permanent.
 * Both triggers have an intervening-if (CR 603.4) on Edgar's location, checked again on resolution,
 * so a trigger does nothing if Edgar has left those zones (e.g. it was killed in response).
 * A Vampire spell of any card type matches, including kindred instants and changelings.
 */
@CardRegistration(set = "INR", collectorNumber = "234")
@CardRegistration(set = "INR", collectorNumber = "328")
@CardRegistration(set = "INR", collectorNumber = "428")
@CardRegistration(set = "C17", collectorNumber = "36")
public class EdgarMarkov extends Card {

    public EdgarMarkov() {
        CreateTokenEffect vampireToken = new CreateTokenEffect("Vampire", 1, 1, CardColor.BLACK,
                List.of(CardSubtype.VAMPIRE), Set.of(), Set.of());

        // Eminence — Whenever you cast another Vampire spell, if Edgar Markov is in the command
        // zone or on the battlefield, create a 1/1 black Vampire creature token.
        CardSubtypePredicate vampireSpell = new CardSubtypePredicate(CardSubtype.VAMPIRE);
        ConditionalEffect eminenceGate = new ConditionalEffect(
                new AnyOf(List.of(new SourceCardInCommandZone(), new SourceCardOnBattlefield())),
                vampireToken);
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                vampireSpell, List.of(eminenceGate)));
        addEffect(EffectSlot.COMMAND_ZONE_ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                vampireSpell, List.of(new ConditionalEffect(new SourceCardInCommandZone(), vampireToken))));

        // Whenever Edgar Markov attacks, put a +1/+1 counter on each Vampire you control.
        addEffect(EffectSlot.ON_ATTACK, new PutCounterOnEachControlledPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE, 1,
                new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE)));
    }
}
