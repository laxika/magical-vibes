package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReveillarkTest extends BaseCardTest {

    private long battlefieldCount(com.github.laxika.magicalvibes.model.Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .count();
    }

    private void killReveillark(Permanent reveillark) {
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, reveillark);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // resolve the leaves-the-battlefield trigger
    }

    @Test
    @DisplayName("LTB returns up to two small creatures from graveyard automatically when two or fewer qualify")
    void returnsTwoSmallCreatures() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LlanowarElves()));
        Permanent reveillark = harness.addToBattlefieldAndReturn(player1, new Reveillark());

        killReveillark(reveillark);

        assertThat(battlefieldCount(player1, "Grizzly Bears")).isEqualTo(1);
        assertThat(battlefieldCount(player1, "Llanowar Elves")).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears") || c.getName().equals("Llanowar Elves"));
    }

    @Test
    @DisplayName("Creatures with power 3 or more are not eligible to return")
    void powerThreeCreatureNotReturned() {
        harness.setGraveyard(player1, List.of(new HillGiant(), new GrizzlyBears()));
        Permanent reveillark = harness.addToBattlefieldAndReturn(player1, new Reveillark());

        killReveillark(reveillark);

        assertThat(battlefieldCount(player1, "Grizzly Bears")).isEqualTo(1);
        assertThat(battlefieldCount(player1, "Hill Giant")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Hill Giant"));
    }

    @Test
    @DisplayName("Reveillark itself (power 4) is not returned by its own trigger")
    void doesNotReturnItself() {
        harness.setGraveyard(player1, List.of());
        Permanent reveillark = harness.addToBattlefieldAndReturn(player1, new Reveillark());

        killReveillark(reveillark);

        assertThat(battlefieldCount(player1, "Reveillark")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Reveillark"));
    }

    @Test
    @DisplayName("With more than two eligible creatures, controller chooses exactly two")
    void choosesTwoOfThree() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LlanowarElves(), new GrizzlyBears()));
        Permanent reveillark = harness.addToBattlefieldAndReturn(player1, new Reveillark());

        killReveillark(reveillark);

        // Controller is prompted to choose which creatures to return (up to two).
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0); // first eligible creature
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0); // second eligible creature

        long returned = battlefieldCount(player1, "Grizzly Bears") + battlefieldCount(player1, "Llanowar Elves");
        assertThat(returned).isEqualTo(2);
        // Exactly one eligible creature stays behind (plus Reveillark).
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears") || c.getName().equals("Llanowar Elves"))
                .hasSize(1);
    }

    @Test
    @DisplayName("Evoke: sacrificed on entry, then returns small creatures from graveyard")
    void evokeSacrificeThenReturn() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Reveillark()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreatureWithEvoke(player1, 0, null);
        harness.passBothPriorities(); // resolve Reveillark -> ETB evoke-sacrifice trigger on stack
        harness.passBothPriorities(); // resolve sacrifice -> LTB trigger on stack
        harness.passBothPriorities(); // resolve LTB trigger -> return small creature

        assertThat(battlefieldCount(player1, "Reveillark")).isZero();
        assertThat(battlefieldCount(player1, "Grizzly Bears")).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Reveillark"));
    }
}
