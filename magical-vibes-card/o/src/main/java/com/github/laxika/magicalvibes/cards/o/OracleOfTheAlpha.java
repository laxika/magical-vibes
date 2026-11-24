package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ConjureSpellbookIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect.SpellbookCard;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MB2", collectorNumber = "259")
@CardRegistration(set = "YDMU", collectorNumber = "4")
public class OracleOfTheAlpha extends Card {

    /** Black Lotus, the five Moxen, Ancestral Recall, Time Walk and Timetwister. */
    private static final List<SpellbookCard> POWER_NINE = List.of(
            new SpellbookCard("LEA", "232"),
            new SpellbookCard("LEA", "263"),
            new SpellbookCard("LEA", "265"),
            new SpellbookCard("LEA", "264"),
            new SpellbookCard("LEA", "262"),
            new SpellbookCard("LEA", "261"),
            new SpellbookCard("LEA", "47"),
            new SpellbookCard("LEA", "83"),
            new SpellbookCard("LEA", "84"));

    public OracleOfTheAlpha() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ConjureSpellbookIntoLibraryEffect(POWER_NINE, 1));
        addTrigger(Triggers.attacks(), new ScryEffect(1));
    }
}
