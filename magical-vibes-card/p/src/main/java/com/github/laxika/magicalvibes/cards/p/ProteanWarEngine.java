package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfSubjectEffect;
import com.github.laxika.magicalvibes.model.effect.CopyExceptions;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookToExileEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YDMU", collectorNumber = "25")
public class ProteanWarEngine extends Card {

    private static final List<DraftFromSpellbookEffect.SpellbookCard> SPELLBOOK = List.of(
            new DraftFromSpellbookEffect.SpellbookCard("DOM", "33"),
            new DraftFromSpellbookEffect.SpellbookCard("M19", "34"),
            new DraftFromSpellbookEffect.SpellbookCard("SCD", "35"),
            new DraftFromSpellbookEffect.SpellbookCard("MID", "16"),
            new DraftFromSpellbookEffect.SpellbookCard("M21", "18"),
            new DraftFromSpellbookEffect.SpellbookCard("AVR", "35"),
            new DraftFromSpellbookEffect.SpellbookCard("M19", "38"),
            new DraftFromSpellbookEffect.SpellbookCard("DOM", "6"),
            new DraftFromSpellbookEffect.SpellbookCard("DMU", "32"),
            new DraftFromSpellbookEffect.SpellbookCard("STX", "165"),
            new DraftFromSpellbookEffect.SpellbookCard("XLN", "137"),
            new DraftFromSpellbookEffect.SpellbookCard("VOW", "170"),
            new DraftFromSpellbookEffect.SpellbookCard("MID", "149"),
            new DraftFromSpellbookEffect.SpellbookCard("KLD", "130"),
            new DraftFromSpellbookEffect.SpellbookCard("M14", "148"));

    public ProteanWarEngine() {
        // "As this enters, draft a card from its spellbook and exile it": the card is exiled face up.
        addStaticEffect(new DraftCardFromSpellbookToExileEffect(SPELLBOOK, false));
        addTrigger(Triggers.becomesCrewed(),
                BecomeCopyOfSubjectEffect.of(Subjects.exiledWithSourceUnlinked(
                                new CardTypePredicate(CardType.CREATURE)))
                        .untilEndOfTurn()
                        .except(CopyExceptions.stillA(CardType.ARTIFACT).alsoWithSubtypes(CardSubtype.VEHICLE)));
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(3), AnimatePermanentsEffect.crew()),
                "Crew 3"));
    }
}
