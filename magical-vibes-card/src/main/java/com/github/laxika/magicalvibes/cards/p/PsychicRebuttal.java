package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.condition.TargetSpellCanBeCountered;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsYouPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ORI", collectorNumber = "67")
public class PsychicRebuttal extends Card {

    public PsychicRebuttal() {
        // Counter target instant or sorcery spell that targets you.
        // Spell mastery — If there are two or more instant and/or sorcery cards in your graveyard,
        // you may copy the spell countered this way. You may choose new targets for the copy.
        //
        // The copy is listed first so the targeted spell is still on the stack when it is copied;
        // TargetSpellCanBeCountered keeps the copy from happening when the counter itself won't
        // (uncounterable spells, Autumn's Veil-style colour protection).
        target(new StackEntryPredicateTargetFilter(
                new StackEntryAllOfPredicate(List.of(
                        new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL)),
                        new StackEntryTargetsYouPredicate()
                )),
                "Target must be an instant or sorcery spell that targets you."))
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new AllOf(List.of(
                                new GraveyardCardThreshold(2, new CardAnyOfPredicate(List.of(
                                        new CardTypePredicate(CardType.INSTANT),
                                        new CardTypePredicate(CardType.SORCERY)
                                ))),
                                new TargetSpellCanBeCountered()
                        )),
                        new MayEffect(new CopySpellEffect(), "Copy the spell countered by Psychic Rebuttal?")))
                .addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
