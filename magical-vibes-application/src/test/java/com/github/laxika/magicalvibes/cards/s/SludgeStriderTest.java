package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MartialCoup;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SludgeStrider.class, Spellbook.class, Shatter.class, Unsummon.class, MartialCoup.class})
@DisplayName("Sludge Strider")
class SludgeStriderTest extends BaseCardTest {

    @Test
    @DisplayName("Another artifact entering lets you pay {1} to drain target player")
    void artifactEnterDrains() {
        harness.addToBattlefield(player1, new SludgeStrider());
        harness.setHand(player1, List.of(new Spellbook()));

        int p1Before = gd.playerLifeTotals.get(player1.getId());
        int p2Before = gd.playerLifeTotals.get(player2.getId());

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // Spellbook resolves and enters, trigger onto stack
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // Trigger resolves and offers the optional payment.

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, p2Before - 1);
        harness.assertLife(player1, p1Before + 1);
    }

    @Test
    @DisplayName("Another artifact leaving lets you pay {1} to drain target player")
    void artifactLeaveDrains() {
        harness.addToBattlefield(player1, new SludgeStrider());
        Permanent book = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        // Opponent destroys your artifact; it was under your control when it left, so the trigger fires.
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);

        int p1Before = gd.playerLifeTotals.get(player1.getId());
        int p2Before = gd.playerLifeTotals.get(player2.getId());

        harness.castInstant(player2, 0, book.getId());
        harness.passBothPriorities(); // Shatter resolves, Spellbook destroyed, trigger enqueued
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // Trigger resolves and offers the optional payment.

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, p2Before - 1);
        harness.assertLife(player1, p1Before + 1);
    }

    @Test
    @DisplayName("Declining the trigger drains no one and spends no mana")
    void declineDrainsNoOne() {
        harness.addToBattlefield(player1, new SludgeStrider());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int p1Before = gd.playerLifeTotals.get(player1.getId());
        int p2Before = gd.playerLifeTotals.get(player2.getId());

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, p1Before);
        harness.assertLife(player2, p2Before);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("An artifact an opponent controls entering does not trigger Sludge Strider")
    void opponentArtifactEnterDoesNotTrigger() {
        harness.addToBattlefield(player1, new SludgeStrider());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Spellbook()));

        int p1Before = gd.playerLifeTotals.get(player1.getId());
        int p2Before = gd.playerLifeTotals.get(player2.getId());

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertLife(player1, p1Before);
        harness.assertLife(player2, p2Before);
    }

    @Test
    @DisplayName("Sludge Strider does not trigger for its own entry")
    void ownEntryDoesNotTrigger() {
        harness.setHand(player1, List.of(new SludgeStrider()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Sludge Strider does not trigger for its own departure")
    void ownDepartureDoesNotTrigger() {
        Permanent strider = harness.addToBattlefieldAndReturn(player1, new SludgeStrider());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, strider.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sludge Strider");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent's artifact leaving does not trigger")
    void opponentArtifactLeaveDoesNotTrigger() {
        harness.addToBattlefield(player1, new SludgeStrider());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new SludgeStrider());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, other.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The controller may target themselves and pays exactly one mana")
    void canTargetController() {
        harness.addToBattlefield(player1, new SludgeStrider());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Both Striders trigger when they leave simultaneously")
    void simultaneousDeparturesTriggerBothStriders() {
        harness.addToBattlefield(player1, new SludgeStrider());
        harness.addToBattlefield(player1, new SludgeStrider());
        harness.setHand(player1, List.of(new MartialCoup()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 5);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Sludge Strider");
        for (int i = 0; i < 2; i++) {
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
            harness.handlePermanentChosen(player1, player2.getId());
        }
        for (int i = 0; i < 2; i++) {
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Returning another artifact to hand triggers the drain")
    void artifactReturnedToHandDrains() {
        harness.addToBattlefield(player1, new SludgeStrider());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new SludgeStrider());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, other.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
