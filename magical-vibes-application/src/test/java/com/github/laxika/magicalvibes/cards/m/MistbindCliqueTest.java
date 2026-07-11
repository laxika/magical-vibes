package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MistbindCliqueTest extends BaseCardTest {

    /** Casts Mistbind Clique, resolves its champion ETB, and champions the given Faerie —
     *  stopping once the "championed" trigger is awaiting a target player. */
    private void championFaerie(UUID faerieId) {
        harness.setHand(player1, List.of(new MistbindClique()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> champion ETB on stack
        harness.passBothPriorities(); // resolve champion ETB -> champion permanent choice
        harness.handlePermanentChosen(player1, faerieId); // champion the Faerie -> championed trigger
    }

    @Test
    @DisplayName("Championing a Faerie prompts a target-player choice")
    void championingPromptsTargetPlayerChoice() {
        UUID faerieId = harness.addToBattlefieldAndReturn(player1, new AvianChangeling()).getId();

        championFaerie(faerieId);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        // Championed creature is exiled; Mistbind Clique stays.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Mistbind Clique"))
                .noneMatch(p -> p.getCard().getName().equals("Avian Changeling"));
    }

    @Test
    @DisplayName("Taps all lands the chosen opponent controls")
    void tapsAllLandsTargetPlayerControls() {
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());
        UUID faerieId = harness.addToBattlefieldAndReturn(player1, new AvianChangeling()).getId();

        championFaerie(faerieId);
        harness.handlePermanentChosen(player1, player2.getId()); // target the opponent
        harness.passBothPriorities(); // resolve the championed trigger

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Island"))
                .allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Target player can be its controller (taps own lands)")
    void canTargetOwnLands() {
        harness.addToBattlefield(player1, new Island());
        UUID faerieId = harness.addToBattlefieldAndReturn(player1, new AvianChangeling()).getId();

        championFaerie(faerieId);
        harness.handlePermanentChosen(player1, player1.getId()); // target self
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Island"))
                .allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Only lands are tapped, not other permanents")
    void tapsOnlyLands() {
        harness.addToBattlefield(player2, new Island());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        UUID faerieId = harness.addToBattlefieldAndReturn(player1, new AvianChangeling()).getId();

        championFaerie(faerieId);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Island"))
                .allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("No championed trigger when there is no Faerie to champion")
    void noTriggerWhenNoFaerie() {
        harness.addToBattlefield(player2, new Island());

        harness.setHand(player1, List.of(new MistbindClique()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> champion ETB on stack
        harness.passBothPriorities(); // resolve champion ETB -> no Faerie -> sacrifice, no trigger

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Mistbind Clique"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Island"))
                .noneMatch(Permanent::isTapped);
    }
}
