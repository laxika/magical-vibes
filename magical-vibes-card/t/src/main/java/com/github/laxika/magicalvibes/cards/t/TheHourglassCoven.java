package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.HoldDraftedSpellbookCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutHeldSpellbookCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "45")
public class TheHourglassCoven extends Card {

    private static final List<DraftFromSpellbookEffect.SpellbookCard> SPELLBOOK = List.of(
            new DraftFromSpellbookEffect.SpellbookCard("HBG", "H24"),
            new DraftFromSpellbookEffect.SpellbookCard("HBG", "H18"),
            new DraftFromSpellbookEffect.SpellbookCard("HBG", "H17"),
            new DraftFromSpellbookEffect.SpellbookCard("HBG", "H20"),
            new DraftFromSpellbookEffect.SpellbookCard("HBG", "H19"),
            new DraftFromSpellbookEffect.SpellbookCard("HBG", "H23"),
            new DraftFromSpellbookEffect.SpellbookCard("HBG", "H25"),
            new DraftFromSpellbookEffect.SpellbookCard("HBG", "H21"),
            new DraftFromSpellbookEffect.SpellbookCard("HBG", "H22"));

    public TheHourglassCoven() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                new DraftCardFromSpellbookEffect(SPELLBOOK, List.of(new HoldDraftedSpellbookCardEffect())),
                new DraftCardFromSpellbookEffect(SPELLBOOK, List.of(new HoldDraftedSpellbookCardEffect())),
                new PutHeldSpellbookCardsOntoBattlefieldEffect()));
        addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.WARLOCK))));
    }
}
