package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Dreadhound.class, DoomBlade.class, Forest.class, GrizzlyBears.class, Millstone.class})
class DreadhoundTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, it mills three cards")
    void entersAndMillsThreeCards() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.enterBattlefieldAndReturn(player1, new Dreadhound());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> (Object) card.getClass())
                .containsExactly(Forest.class, Forest.class, Forest.class);
    }

    @Test
    @DisplayName("Whenever a creature dies, each opponent loses one life")
    void creatureDeathMakesEachOpponentLoseLife() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addToBattlefield(player1, new Dreadhound());
        resolveAllTriggers();

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, bears.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Each creature card put into any library's graveyard costs opponents one life")
    void creatureCardsFromOpponentsLibraryMakeEachOpponentLoseLife() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addToBattlefield(player1, new Dreadhound());
        resolveAllTriggers();

        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addToBattlefield(player1, new Millstone());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Its own enter trigger drains once for each creature card milled")
    void enterTriggerDrainsForEachCreatureCard() {
        harness.setLibrary(player1, List.of(new Dreadhound(), new Forest(), new Dreadhound(), new Forest()));

        harness.enterBattlefieldAndReturn(player1, new Dreadhound());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A short library mills only its remaining cards")
    void enterTriggerMillsShortLibrary() {
        harness.setLibrary(player1, List.of(new Dreadhound()));

        harness.enterBattlefieldAndReturn(player1, new Dreadhound());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Dreadhound triggers for its own death")
    void ownDeathMakesOpponentLoseLife() {
        Permanent dreadhound = harness.addToBattlefieldAndReturn(player1, new Dreadhound());
        dreadhound.setMarkedDamage(6);

        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Dreadhound");
        harness.assertNotOnBattlefield(player1, "Dreadhound");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Dreadhound sees its own death and the death of an opposing creature simultaneously")
    void simultaneousDeathsIncludeDreadhoundItself() {
        Permanent dreadhound = harness.addToBattlefieldAndReturn(player1, new Dreadhound());
        Permanent opposingDreadhound = harness.addToBattlefieldAndReturn(player2, new Dreadhound());
        dreadhound.setMarkedDamage(6);
        opposingDreadhound.setMarkedDamage(6);

        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Dreadhound");
        harness.assertInGraveyard(player2, "Dreadhound");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A surviving Dreadhound sees an allied creature die")
    void alliedCreatureDeathMakesOpponentLoseLife() {
        harness.addToBattlefield(player1, new Dreadhound());
        Permanent dyingDreadhound = harness.addToBattlefieldAndReturn(player1, new Dreadhound());
        dyingDreadhound.setMarkedDamage(6);

        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Entering with an empty library causes no life loss")
    void enterTriggerWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new Dreadhound());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Milling only noncreature cards causes no life loss")
    void millingNoncreatureCardsDoesNotDrain() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.enterBattlefieldAndReturn(player1, new Dreadhound());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
