package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceCardInCommandZone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

/**
 * Eminence (CR 902.3) is a command-zone keyword ability: the spell-cast trigger below functions
 * while this card is in its controller's command zone, and the same ability works while it is a
 * battlefield permanent. The command-zone trigger is paired with a {@code SourceCardInCommandZone}
 * intervening-if so it fails if the card left the command zone (e.g. cast in response).
 */
@CardRegistration(set = "INR", collectorNumber = "234")
public class EdgarMarkov extends Card {

    public EdgarMarkov() {
        CreateTokenEffect vampireToken = new CreateTokenEffect("Vampire", 1, 1, CardColor.BLACK,
                List.of(CardSubtype.VAMPIRE), Set.of(), Set.of());

        // Eminence — Whenever you cast another Vampire spell, if Edgar Markov is in the command
        // zone or on the battlefield, create a 1/1 black Vampire creature token.
        SpellCastTriggerEffect battlefieldTrigger = new SpellCastTriggerEffect(
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardSubtypePredicate(CardSubtype.VAMPIRE))),
                List.of(vampireToken));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, battlefieldTrigger);
        addEffect(EffectSlot.COMMAND_ZONE_ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardSubtypePredicate(CardSubtype.VAMPIRE))),
                List.of(new ConditionalEffect(new SourceCardInCommandZone(), vampireToken))));

        // Whenever Edgar Markov attacks, put a +1/+1 counter on each Vampire you control.
        addEffect(EffectSlot.ON_ATTACK, new PutCounterOnEachControlledPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE, 1,
                new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE)));
    }
}
