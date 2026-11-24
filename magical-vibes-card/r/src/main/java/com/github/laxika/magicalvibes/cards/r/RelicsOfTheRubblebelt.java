package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

import java.util.List;

@CardRegistration(set = "YMKM", collectorNumber = "19")
public class RelicsOfTheRubblebelt extends Card {

    private static final List<DraftFromSpellbookEffect.SpellbookCard> SPELLBOOK = List.of(
            new DraftFromSpellbookEffect.SpellbookCard("DIS", "159"),
            new DraftFromSpellbookEffect.SpellbookCard("RAV", "255"),
            new DraftFromSpellbookEffect.SpellbookCard("RAV", "260"),
            new DraftFromSpellbookEffect.SpellbookCard("RAV", "262"),
            new DraftFromSpellbookEffect.SpellbookCard("GPT", "150"),
            new DraftFromSpellbookEffect.SpellbookCard("GPT", "152"),
            new DraftFromSpellbookEffect.SpellbookCard("GPT", "155"),
            new DraftFromSpellbookEffect.SpellbookCard("DIS", "165"),
            new DraftFromSpellbookEffect.SpellbookCard("RAV", "270"),
            new DraftFromSpellbookEffect.SpellbookCard("DIS", "166"));

    public RelicsOfTheRubblebelt() {
        addSpellEffect(new DraftCardFromSpellbookEffect(SPELLBOOK).times(2));
        addSpellEffect(ChooseCardEffect.oneOf(Subjects.cardsThisWay(StepKind.CONJURE),
                "one of those cards to put onto the battlefield tapped"));
        addSpellEffect(new PutCardsOntoBattlefieldEffect(Subjects.cardsThisWay(StepKind.CHOOSE)).enteringTapped());
    }
}
