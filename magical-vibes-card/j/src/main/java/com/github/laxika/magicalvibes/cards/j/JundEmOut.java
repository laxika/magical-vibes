package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.b.Blightning;
import com.github.laxika.magicalvibes.cards.t.Tarmogoyf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Retrace;
import com.github.laxika.magicalvibes.model.effect.CreateRandomCardCopyAndMayCastEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect.SpellbookCard;

import java.util.List;

@CardRegistration(set = "MB2", collectorNumber = "357")
@CardRegistration(set = "MB2", collectorNumber = "596")
public class JundEmOut extends Card {

    /** Abrupt Decay, Blightning, Bloodbraid Elf, Lightning Bolt, Liliana of the Veil, Tarmogoyf. */
    private static final List<SpellbookCard> JUND_CARDS = List.of(
            new SpellbookCard("RTR", "141"),
            new SpellbookCard("ALA", "156"),
            new SpellbookCard("ARB", "50"),
            new SpellbookCard("M10", "146"),
            new SpellbookCard("ISD", "105"),
            new SpellbookCard("FUT", "153"));

    public JundEmOut() {
        addSpellEffect(new CreateRandomCardCopyAndMayCastEffect(JUND_CARDS));
        addCastingOption(new Retrace());
    }
}
