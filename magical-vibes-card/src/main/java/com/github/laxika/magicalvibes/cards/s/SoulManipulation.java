package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ARB", collectorNumber = "29")
public class SoulManipulation extends Card {

    public SoulManipulation() {
        // "Choose one or both" is modelled as choose-one among three options: each single mode
        // and a combined "both" mode (see Remember the Fallen). Mode 0 targets a creature spell on
        // the stack (counter); mode 1 targets a creature card in your graveyard (interactive choice).
        // The "both" mode carries the counter's spell target through the graveyard-choice flow.
        TargetFilter creatureSpell = new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL)),
                "Target must be a creature spell."
        );

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Counter target creature spell",
                        new CounterSpellEffect(),
                        creatureSpell),
                new ChooseOneEffect.ChooseOneOption(
                        "Return target creature card from your graveyard to your hand",
                        new ReturnTargetCardsFromGraveyardToHandEffect(
                                new CardTypePredicate(CardType.CREATURE), 1)),
                new ChooseOneEffect.ChooseOneOption(
                        "Counter target creature spell and return target creature card from your graveyard to your hand",
                        List.<CardEffect>of(
                                new CounterSpellEffect(),
                                new ReturnTargetCardsFromGraveyardToHandEffect(
                                        new CardTypePredicate(CardType.CREATURE), 1)),
                        creatureSpell)
        )));
    }
}
