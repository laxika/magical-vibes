package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CopperlineGorge;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Glimmerpost.class, CopperlineGorge.class})
class GlimmerpostTest extends BaseCardTest {

    @Test
    @DisplayName("Playing Glimmerpost puts ETB trigger on the stack")
    void playingPutsEtbTriggerOnStack() {
        playGlimmerpost(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("ETB gains 1 life when Glimmerpost is the only Locus")
    void etbGainsOneLifeWithSingleLocus() {
        harness.setLife(player1, 20);
        playGlimmerpost(player1);
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("ETB gains 2 life when a second Locus is already on the battlefield")
    void etbGainsTwoLifeWithTwoLoci() {
        harness.addToBattlefield(player1, new Glimmerpost());
        harness.setLife(player1, 20);

        playGlimmerpost(player1);
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("ETB counts Loci controlled by all players")
    void etbCountsAllPlayersLoci() {
        harness.addToBattlefield(player2, new Glimmerpost());
        harness.setLife(player1, 20);

        playGlimmerpost(player1);
        harness.passBothPriorities(); // resolve ETB trigger

        // Counts opponent's Locus + own Glimmerpost = 2
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("ETB gains 3 life with three Loci on the battlefield")
    void etbGainsThreeLifeWithThreeLoci() {
        harness.addToBattlefield(player1, new Glimmerpost());
        harness.addToBattlefield(player2, new Glimmerpost());
        harness.setLife(player1, 20);

        playGlimmerpost(player1);
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Game log records life gain from ETB")
    void gameLogRecordsLifeGain() {
        playGlimmerpost(player1);
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("gains 1 life"));
    }

    @Test
    @DisplayName("Glimmerpost enters the battlefield as a permanent")
    void entersBattlefieldAsPermanent() {
        playGlimmerpost(player1);
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertOnBattlefield(player1, "Glimmerpost");
    }

    @Test
    @DisplayName("Stack is empty after ETB fully resolves")
    void stackEmptyAfterResolution() {
        playGlimmerpost(player1);
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tap adds one colorless mana immediately while the ETB trigger is pending")
    void tapAddsColorlessManaWithoutUsingStack() {
        playGlimmerpost(player1);

        harness.tapPermanent(player1, 0);

        assertThat(findPermanent(player1, "Glimmerpost").isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 20);

        harness.passBothPriorities();
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("ETB ignores non-Locus lands and Loci outside the battlefield")
    void countsOnlyBattlefieldLoci() {
        harness.addToBattlefield(player1, new CopperlineGorge());
        harness.addToBattlefield(player2, new CopperlineGorge());
        harness.setHand(player2, List.of(new Glimmerpost()));
        harness.setGraveyard(player1, List.of(new Glimmerpost()));
        playGlimmerpost(player1);

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("ETB includes Loci that arrive after the trigger is created")
    void countsLociAtResolution() {
        playGlimmerpost(player1);
        harness.addToBattlefield(player2, new Glimmerpost());

        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("ETB resolves after its source leaves and counts only remaining Loci")
    void sourceLeavingDoesNotPreventLifeGain() {
        harness.addToBattlefield(player2, new Glimmerpost());
        playGlimmerpost(player1);
        harness.getPermanentRemovalService().removePermanentToGraveyard(
                gd, findPermanent(player1, "Glimmerpost"));

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Glimmerpost");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB gains no life when no Loci remain at resolution")
    void gainsNoLifeWhenNoLociRemain() {
        playGlimmerpost(player1);
        harness.getPermanentRemovalService().removePermanentToGraveyard(
                gd, findPermanent(player1, "Glimmerpost"));

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    private void playGlimmerpost(Player player) {
        harness.setHand(player, List.of(new Glimmerpost()));
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player, 0);
    }
}
