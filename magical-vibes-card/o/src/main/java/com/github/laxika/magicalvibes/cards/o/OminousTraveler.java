package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantAnyColorManaAndSelfCastAbilityToCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "62")
public class OminousTraveler extends Card {

    private static final List<DraftFromSpellbookEffect.SpellbookCard> SPELLBOOK = List.of(
            new DraftFromSpellbookEffect.SpellbookCard("VOW", "154"),
            new DraftFromSpellbookEffect.SpellbookCard("MID", "249"),
            new DraftFromSpellbookEffect.SpellbookCard("MID", "123"),
            new DraftFromSpellbookEffect.SpellbookCard("MID", "137"),
            new DraftFromSpellbookEffect.SpellbookCard("VOW", "229"),
            new DraftFromSpellbookEffect.SpellbookCard("VOW", "205"),
            new DraftFromSpellbookEffect.SpellbookCard("MID", "246"),
            new DraftFromSpellbookEffect.SpellbookCard("MID", "69"),
            new DraftFromSpellbookEffect.SpellbookCard("MID", "74"),
            new DraftFromSpellbookEffect.SpellbookCard("VOW", "80"),
            new DraftFromSpellbookEffect.SpellbookCard("VOW", "19"),
            new DraftFromSpellbookEffect.SpellbookCard("VOW", "93"),
            new DraftFromSpellbookEffect.SpellbookCard("MID", "91"),
            new DraftFromSpellbookEffect.SpellbookCard("VOW", "118"),
            new DraftFromSpellbookEffect.SpellbookCard("MID", "212"));

    public OminousTraveler() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DraftCardFromSpellbookEffect(SPELLBOOK, List.of(
                        new PerpetuallyGrantAnyColorManaAndSelfCastAbilityToCardEffect(
                                new ReturnPermanentControlledByPlayerToHandEffect(
                                        new PermanentAllOfPredicate(List.of(
                                                new PermanentIsCreaturePredicate(),
                                                new PermanentNamedPredicate("Ominous Traveler"))),
                                        "creature")))));
    }
}
