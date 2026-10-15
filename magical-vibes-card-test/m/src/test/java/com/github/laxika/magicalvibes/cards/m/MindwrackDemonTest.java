package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Catalog;
import com.github.laxika.magicalvibes.cards.c.ChaplainsBlessing;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WickerWitch;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MindwrackDemon.class, Forest.class, GrizzlyBears.class, Naturalize.class,
        Pacifism.class, Shock.class, Catalog.class, ChaplainsBlessing.class, WickerWitch.class})
class MindwrackDemonTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield and mills four cards")
    void millsFourCardsOnEnter() {
        harness.setLibrary(player1, library(5));
        harness.setHand(player1, List.of(new MindwrackDemon()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Loses four life during upkeep without delirium")
    void losesLifeWithoutDelirium() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new MindwrackDemon());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Does not lose life during upkeep with delirium")
    void doesNotLoseLifeWithDelirium() {
        harness.setLife(player1, 20);
        setDelirium();
        harness.addToBattlefield(player1, new MindwrackDemon());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Checks delirium when the upkeep ability resolves")
    void checksDeliriumAtResolution() {
        harness.setLife(player1, 20);
        setDelirium();
        harness.addToBattlefield(player1, new MindwrackDemon());

        advanceToUpkeep(player1);
        gd.playerGraveyards.get(player1.getId()).removeLast();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Does not lose life if delirium is gained before resolution")
    void gainsDeliriumBeforeResolution() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new MindwrackDemon());

        advanceToUpkeep(player1);
        setDelirium();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Mills all remaining cards when fewer than four remain")
    void millsShortLibrary() {
        harness.setLibrary(player1, library(2));
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new MindwrackDemon()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertOnBattlefield(player1, "Mindwrack Demon");
    }

    @Test
    @DisplayName("Four cards with only three distinct card types do not satisfy delirium")
    void countsDistinctCardTypesRatherThanCards() {
        harness.setLife(player1, 20);
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Naturalize()));
        harness.addToBattlefield(player1, new MindwrackDemon());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("An opponent's delirium does not prevent the controller's life loss")
    void ignoresOpponentsGraveyard() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Pacifism()));
        harness.addToBattlefield(player1, new MindwrackDemon());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerOnOpponentsUpkeep() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new MindwrackDemon());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The upkeep trigger still resolves after the Demon leaves the battlefield")
    void upkeepTriggerSurvivesSourceLeaving() {
        harness.setLife(player1, 20);
        harness.setGraveyard(player1, List.of());
        harness.addToBattlefield(player1, new MindwrackDemon());

        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("An artifact creature contributes two card types to delirium")
    void countsAllTypesOfMultitypeCard() {
        harness.setLife(player1, 20);
        harness.setGraveyard(player1, List.of(
                new WickerWitch(), new Catalog(), new ChaplainsBlessing()));
        harness.addToBattlefield(player1, new MindwrackDemon());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
    private void setDelirium() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Pacifism()));
    }

    private List<Card> library(int size) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            cards.add(new Naturalize());
        }
        return cards;
    }
}
