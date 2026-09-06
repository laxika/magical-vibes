package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StoneGiant.class, GrizzlyBears.class, LlanowarElves.class, GiantGrowth.class, Terror.class})
class StoneGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Grants flying to target creature you control with toughness less than its power")
    void grantsFlyingToTargetCreature() {
        addCreatureReady(player1, new StoneGiant());
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

        addCreatureReady(player1, new StoneGiant());
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
    @DisplayName("Waits for priority before destroying the target at the next end step")
    void waitsForPriorityBeforeDelayedDestruction() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        addCreatureReady(player1, new StoneGiant());
        Permanent elves = addCreatureReady(player1, new LlanowarElves());

        harness.activateAbility(player1, 0, null, elves.getId());
        harness.passBothPriorities();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cannot target creature with toughness equal to or greater than Stone Giant's power")
    void cannotTargetHighToughnessCreature() {
        addCreatureReady(player1, new StoneGiant());

        // Stone Giant is 3/4; another Stone Giant has toughness 4, which is not less than 3
        Permanent anotherGiant = addCreatureReady(player1, new StoneGiant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, anotherGiant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target opponent's creatures")
    void cannotTargetOpponentCreatures() {
        addCreatureReady(player1, new StoneGiant());
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
    @DisplayName("Uses Stone Giant's last known power if it leaves before the ability resolves")
    void usesLastKnownPowerIfSourceLeavesBeforeResolution() {
        Permanent stoneGiant = addCreatureReady(player1, new StoneGiant());
        Permanent elves = addCreatureReady(player1, new LlanowarElves());

        harness.activateAbility(player1, 0, null, elves.getId());

        harness.setHand(player2, List.of(new Terror()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, stoneGiant.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Stone Giant");

        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(gd, elves.getId())).isNotNull();
        assertThat(elves.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Does nothing if the target's toughness is no longer less than Stone Giant's power")
    void doesNothingWhenTargetBecomesTooToughBeforeResolution() {
        Permanent stoneGiant = addCreatureReady(player1, new StoneGiant());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);

        harness.passBothPriorities();

        assertThat(bears.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(stoneGiant.isTapped()).isTrue();
    }
}
