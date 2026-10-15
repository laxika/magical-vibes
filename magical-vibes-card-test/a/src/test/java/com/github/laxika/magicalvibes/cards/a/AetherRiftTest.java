package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.EverybodyLives;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.l.LibraryOfLeng;
import com.github.laxika.magicalvibes.cards.u.UndergroundRiver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AetherRift.class, GrizzlyBears.class, LlanowarElves.class, UndergroundRiver.class,
        LibraryOfLeng.class, EverybodyLives.class})
class AetherRiftTest extends BaseCardTest {

    @Test
    @DisplayName("A creature discarded at upkeep returns unless a player pays 5 life")
    void creatureReturnsWhenNobodyPays() {
        harness.addToBattlefield(player1, new AetherRift());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A player paying 5 life prevents the discarded creature from returning")
    void paymentPreventsReturn() {
        harness.addToBattlefield(player1, new AetherRift());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A noncreature discarded at upkeep is not returned or offered a payment")
    void noncreatureIsNotReturned() {
        harness.addToBattlefield(player1, new AetherRift());
        harness.setHand(player1, List.of(new UndergroundRiver()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Underground River");
    }

    @Test
    @DisplayName("An empty hand produces no discard or payment choice")
    void emptyHandDoesNothing() {
        harness.addToBattlefield(player1, new AetherRift());
        harness.setHand(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A player unable to pay is skipped and the next player can pay")
    void unablePlayerIsSkipped() {
        harness.addToBattlefield(player1, new AetherRift());
        harness.setHand(player1, List.of(new LlanowarElves()));
        harness.setLife(player1, 4);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    void controllerCanPayAndNoOtherPlayerIsAsked() {
        harness.addToBattlefield(player1, new AetherRift());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void creatureReturnsImmediatelyWhenNeitherPlayerCanPay() {
        harness.addToBattlefield(player1, new AetherRift());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 4);
        harness.setLife(player2, 4);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player1, 4);
        harness.assertLife(player2, 4);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new AetherRift());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void decliningLibraryOfLengReplacementAllowsCreatureToReturn() {
        harness.addToBattlefield(player1, new AetherRift());
        harness.addToBattlefield(player1, new LibraryOfLeng());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLife(player2, 4);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player1, 20);
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(card -> card instanceof GrizzlyBears);
    }

    @Test
    void acceptingLibraryOfLengReplacementDoesNotOfferLifePayment() {
        harness.addToBattlefield(player1, new AetherRift());
        harness.addToBattlefield(player1, new LibraryOfLeng());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isInstanceOf(GrizzlyBears.class);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void playersWhoCannotLoseLifeCannotPreventReturnByPayingLife() {
        harness.addToBattlefield(player1, new AetherRift());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new EverybodyLives()));

        advanceToUpkeep(player1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
