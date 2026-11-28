package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfRandomCardEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect.SpellbookCard;

import java.util.List;

@CardRegistration(set = "MB2", collectorNumber = "270")
@CardRegistration(set = "MB2", collectorNumber = "506")
public class WhosThatPraetor extends Card {

    /**
     * Elesh Norn, Mother of Machines; Jin-Gitaxias, Progress Tyrant; Sheoldred, the Apocalypse; Urabrask, Heretic
     * Praetor; Vorinclex, Monstrous Raider; Ebon Praetor.
     */
    private static final List<SpellbookCard> PRAETORS = List.of(
            new SpellbookCard("ONE", "10"),
            new SpellbookCard("NEO", "59"),
            new SpellbookCard("DMU", "107"),
            new SpellbookCard("SNC", "129"),
            new SpellbookCard("KHM", "199"),
            new SpellbookCard("FEM", "37"));

    public WhosThatPraetor() {
        addSpellEffect(new CreateTokenCopyOfRandomCardEffect(PRAETORS));
    }
}
