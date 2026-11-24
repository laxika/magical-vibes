package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect.SpellbookCard;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YDMU", collectorNumber = "5")
public class SkywriterDjinn extends Card {

    private static final List<SpellbookCard> SPELLBOOK = List.of(
            new SpellbookCard("M21", "69"),
            new SpellbookCard("WAR", "72"),
            new SpellbookCard("RIX", "38"),
            new SpellbookCard("IKO", "56"),
            new SpellbookCard("MH2", "46"),
            new SpellbookCard("AVR", "56"),
            new SpellbookCard("MID", "78"),
            new SpellbookCard("IKO", "55"),
            new SpellbookCard("SCG", "52"),
            new SpellbookCard("WAR", "46"),
            new SpellbookCard("SNC", "48"),
            new SpellbookCard("DOM", "45"),
            new SpellbookCard("STX", "39"),
            new SpellbookCard("ONS", "74"),
            new SpellbookCard("NEO", "81"));

    public SkywriterDjinn() {
        // "Then if you have fewer cards in hand than the number of basic land types among lands you control, repeat
        // this process."
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                ConjureCardsEffect.atRandomFrom(SPELLBOOK, ConjureDestination.intoHand())
                        .repeatingWhileHandSmallerThan(new BasicLandTypesAmongControlledLands()));
    }
}
