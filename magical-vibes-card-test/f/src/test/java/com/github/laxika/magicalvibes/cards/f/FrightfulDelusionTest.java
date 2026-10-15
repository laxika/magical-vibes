package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.d.DarkthicketWolf;
import com.github.laxika.magicalvibes.cards.a.AvacynsPilgrim;
import com.github.laxika.magicalvibes.cards.o.ObstinateBaloth;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrightfulDelusion.class, DarkthicketWolf.class, AvacynsPilgrim.class, ObstinateBaloth.class})
class FrightfulDelusionTest extends BaseCardTest {

    @Test
    @DisplayName("Counters spell and opponent discards when they cannot pay {1}")
    void countersAndDiscardsWhenCannotPay() {
        DarkthicketWolf bears = new DarkthicketWolf();
        DarkthicketWolf bearsInHand = new DarkthicketWolf();
        harness.setHand(player1, List.of(bears, bearsInHand));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new FrightfulDelusion()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Darkthicket Wolf");
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(bears.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player1.getId());

        harness.handleCardChosen(player1, 0);

        // Spell is countered (opponent had no mana left to pay {1})
        harness.assertInGraveyard(player1, "Darkthicket Wolf");
        harness.assertNotOnBattlefield(player1, "Darkthicket Wolf");
    }

    @Test
    @DisplayName("Opponent discards and spell is not countered when they pay {1}")
    void discardsAndNotCounteredWhenPays() {
        AvacynsPilgrim elves = new AvacynsPilgrim();
        DarkthicketWolf bearsInHand = new DarkthicketWolf();
        harness.setHand(player1, List.of(elves, bearsInHand));
        harness.addMana(player1, ManaColor.GREEN, 2); // 1 to cast, 1 to pay

        harness.setHand(player2, List.of(new FrightfulDelusion()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.assertInHand(player1, "Darkthicket Wolf");
        harness.assertNotInGraveyard(player1, "Darkthicket Wolf");

        // Opponent pays
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.assertInGraveyard(player1, "Darkthicket Wolf");

        // Elves should not be countered
        harness.assertNotInGraveyard(player1, "Avacyn's Pilgrim");

        // Resolve the elves spell
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Avacyn's Pilgrim");
    }

    @Test
    @DisplayName("Opponent discards and spell is countered when they decline to pay")
    void discardsAndCounteredWhenDeclines() {
        AvacynsPilgrim elves = new AvacynsPilgrim();
        DarkthicketWolf bearsInHand = new DarkthicketWolf();
        harness.setHand(player1, List.of(elves, bearsInHand));
        harness.addMana(player1, ManaColor.GREEN, 2); // 1 to cast, 1 available

        harness.setHand(player2, List.of(new FrightfulDelusion()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Avacyn's Pilgrim");
        harness.assertNotOnBattlefield(player1, "Avacyn's Pilgrim");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.assertInGraveyard(player1, "Darkthicket Wolf");
    }

    @Test
    @DisplayName("Spell is countered even when opponent has no cards to discard")
    void countersWhenOpponentHasEmptyHand() {
        AvacynsPilgrim elves = new AvacynsPilgrim();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new FrightfulDelusion()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());
        harness.passBothPriorities();

        // Opponent has no cards in hand (elves was cast), no mana to pay
        // Both effects resolve without interaction
        harness.assertInGraveyard(player1, "Avacyn's Pilgrim");
        harness.assertNotOnBattlefield(player1, "Avacyn's Pilgrim");
    }

    @Test
    @DisplayName("Fizzles if target spell is no longer on the stack — no discard")
    void fizzlesIfTargetSpellRemoved() {
        DarkthicketWolf bears = new DarkthicketWolf();
        DarkthicketWolf bearsInHand = new DarkthicketWolf();
        harness.setHand(player1, List.of(bears, bearsInHand));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new FrightfulDelusion()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());

        // Remove target from stack before Frightful Delusion resolves
        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getName().equals("Darkthicket Wolf"));

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        // No discard happened
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Frightful Delusion goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        AvacynsPilgrim elves = new AvacynsPilgrim();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new FrightfulDelusion()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Frightful Delusion");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty hand does not prevent paying to keep the spell")
    void canPayWithEmptyHand() {
        AvacynsPilgrim pilgrim = new AvacynsPilgrim();
        harness.setHand(player1, List.of(pilgrim));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new FrightfulDelusion()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, pilgrim.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.assertInGraveyard(player2, "Frightful Delusion");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Avacyn's Pilgrim");
    }

    @Test
    @DisplayName("Discard from your own Frightful Delusion does not invoke an opponent-only replacement")
    void ownSpellDiscardDoesNotPutBalothOntoBattlefield() {
        AvacynsPilgrim pilgrim = new AvacynsPilgrim();
        harness.setHand(player1, List.of(pilgrim, new FrightfulDelusion(), new ObstinateBaloth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.ensurePriority(player1);
        harness.castInstant(player1, 0, pilgrim.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Obstinate Baloth");
        harness.assertNotOnBattlefield(player1, "Obstinate Baloth");
        harness.assertInGraveyard(player1, "Avacyn's Pilgrim");
    }
}
