package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StoneGiant.class, GrizzlyBears.class, LlanowarElves.class})
class StoneGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Grants flying to target creature you control with toughness less than its power")
    void grantsFlyingToTargetCreature() {
        Permanent stoneGiant = addCreatureReady(player1, new StoneGiant());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        Permanent targetAfter = gqs.findPermanentById(gd, bears.getId());
        assertThat(targetAfter).isNotNull();
        assertThat(targetAfter.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Target creature is destroyed at the beginning of the next end step")
    void destroysTargetAtEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        Permanent stoneGiant = addCreatureReady(player1, new StoneGiant());
        Permanent elves = addCreatureReady(player1, new LlanowarElves());

        harness.activateAbility(player1, 0, null, elves.getId());
        harness.passBothPriorities();

        // Elves should still be on battlefield with flying
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gqs.findPermanentById(gd, elves.getId()).getGrantedKeywords()).contains(Keyword.FLYING);

        // Advance to end step — elves should be destroyed (turn may auto-advance past end step)
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cannot target creature with toughness equal to or greater than Stone Giant's power")
    void cannotTargetHighToughnessCreature() {
        Permanent stoneGiant = addCreatureReady(player1, new StoneGiant());

        Permanent anotherGiant = addCreatureReady(player1, new StoneGiant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, anotherGiant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target opponent's creatures")
    void cannotTargetOpponentCreatures() {
        Permanent stoneGiant = addCreatureReady(player1, new StoneGiant());

        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Stone Giant taps when ability is activated")
    void tapsWhenAbilityActivated() {
        Permanent stoneGiant = addCreatureReady(player1, new StoneGiant());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(stoneGiant.isTapped()).isFalse();
        harness.activateAbility(player1, 0, null, bears.getId());
        assertThat(stoneGiant.isTapped()).isTrue();
    }

    @Test
    void usesLastKnownPowerIfSourceLeavesBeforeResolution() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        Permanent stoneGiant = addCreatureReady(player1, new StoneGiant());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, stoneGiant));
        harness.passBothPriorities();

        Permanent targetAfter = gqs.findPermanentById(gd, bears.getId());
        assertThat(targetAfter).isNotNull();
        assertThat(targetAfter.getGrantedKeywords()).contains(Keyword.FLYING);
    }
}
