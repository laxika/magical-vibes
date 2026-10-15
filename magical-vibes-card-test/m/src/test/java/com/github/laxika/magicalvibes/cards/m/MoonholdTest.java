package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Moonhold.class, Forest.class, GrizzlyBears.class})
class MoonholdTest extends BaseCardTest {

    /** Player1 casts Moonhold at player2 on player1's turn, paying the given mana. */
    private void castMoonholdAtPlayer2(int red, int white) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Moonhold()));
        if (red > 0) harness.addMana(player1, ManaColor.RED, red);
        if (white > 0) harness.addMana(player1, ManaColor.WHITE, white);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    /**
     * Hand indices player2 could play right now on their own main phase, with a land at index 0
     * (needs no mana) and an affordable creature at index 1.
     */
    private List<Integer> player2Playable() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player2, List.of(new Forest(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.clearPriorityPassed();
        harness.ensurePriority(player2);
        return harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(harness.getGameData(), player2.getId());
    }

    @Test
    @DisplayName("{R} spent: target can't play lands but can still cast creatures")
    void redSpentBlocksLands() {
        castMoonholdAtPlayer2(3, 0); // {2}{R/W} all red → only {R} spent

        List<Integer> playable = player2Playable();
        assertThat(playable).doesNotContain(0); // land blocked
        assertThat(playable).contains(1);        // creature still castable
    }

    @Test
    @DisplayName("{W} spent: target can't cast creatures but can still play lands")
    void whiteSpentBlocksCreatures() {
        castMoonholdAtPlayer2(0, 3); // {2}{R/W} all white → only {W} spent

        List<Integer> playable = player2Playable();
        assertThat(playable).contains(0);        // land still playable
        assertThat(playable).doesNotContain(1);  // creature blocked
    }

    @Test
    @DisplayName("{R} and {W} both spent: target can neither play lands nor cast creatures")
    void bothColorsBlockBoth() {
        // {2} generic split across colors and the {R/W} hybrid the other → both {R} and {W} spent.
        castMoonholdAtPlayer2(2, 2);

        List<Integer> playable = player2Playable();
        assertThat(playable).doesNotContain(0);
        assertThat(playable).doesNotContain(1);
    }

    @Test
    @DisplayName("Restriction wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        castMoonholdAtPlayer2(3, 0);
        assertThat(player2Playable()).doesNotContain(0);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(player2Playable()).contains(0);
    }

    @Test
    @DisplayName("Creature restriction also wears off at end of turn")
    void creatureRestrictionWearsOffAtEndOfTurn() {
        castMoonholdAtPlayer2(0, 3);
        assertThat(player2Playable()).doesNotContain(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(player2Playable()).contains(1);
        harness.castCreature(player2, 1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Both restrictions prevent actual land plays and creature casts")
    void restrictedActionsAreRejected() {
        castMoonholdAtPlayer2(2, 2);
        assertThat(player2Playable()).doesNotContain(0, 1);

        assertThatThrownBy(() -> harness.playLand(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castCreature(player2, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Moonhold can target its caster and leaves the other player unrestricted")
    void canTargetSelf() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of(new Moonhold()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castCreature(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(player2Playable()).contains(0, 1);
    }

    @Test
    @DisplayName("The creature restriction does not prevent casting an instant")
    void canStillCastInstants() {
        castMoonholdAtPlayer2(0, 3);
        harness.setHand(player2, List.of(new Moonhold()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Moonhold");
        harness.setHand(player1, List.of(new Forest()));
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
