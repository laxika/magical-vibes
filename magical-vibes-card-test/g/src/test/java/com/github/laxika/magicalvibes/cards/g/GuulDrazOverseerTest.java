package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.ReefShaman;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GuulDrazOverseer.class, GrizzlyBears.class, Forest.class, Swamp.class, ReefShaman.class})
class GuulDrazOverseerTest extends BaseCardTest {

    @Test
    @DisplayName("A non-Swamp land gives other creatures +1/+0")
    void nonSwampLandBoostsOtherCreatures() {
        Permanent overseer = harness.addToBattlefieldAndReturn(player1, new GuulDrazOverseer());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(overseer.getEffectivePower()).isEqualTo(3);
        assertThat(otherCreature.getEffectivePower()).isEqualTo(3);
        assertThat(otherCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("A Swamp gives other creatures +2/+0")
    void swampBoostsOtherCreaturesTwice() {
        Permanent overseer = harness.addToBattlefieldAndReturn(player1, new GuulDrazOverseer());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Swamp()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(overseer.getEffectivePower()).isEqualTo(3);
        assertThat(otherCreature.getEffectivePower()).isEqualTo(4);
        assertThat(otherCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's land does not trigger Guul Draz Overseer")
    void opponentLandDoesNotTrigger() {
        Permanent overseer = harness.addToBattlefieldAndReturn(player1, new GuulDrazOverseer());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Swamp()));
        harness.forceActivePlayer(player2);

        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(overseer.getEffectivePower()).isEqualTo(3);
        assertThat(otherCreature.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GuulDrazOverseer());
        harness.setHand(player1, List.of(new Swamp()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(otherCreature.getEffectivePower()).isEqualTo(4);

        harness.passUntil(TurnStep.CLEANUP);

        assertThat(otherCreature.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("A Forest changed into a Swamp before landfall resolves gives +2/+0")
    void landBecomingSwampBeforeResolutionGetsLargerBoost() {
        Permanent shaman = addCreatureReady(player1, new ReefShaman());
        Permanent overseer = harness.addToBattlefieldAndReturn(player1, new GuulDrazOverseer());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        Permanent land = findPermanent(player1, "Forest");

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(shaman),
                null, land.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "SWAMP");
        harness.passBothPriorities();

        assertThat(overseer.getEffectivePower()).isEqualTo(3);
        assertThat(otherCreature.getEffectivePower()).isEqualTo(4);
        assertThat(otherCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("A Swamp changed into a Forest before landfall resolves gives only +1/+0")
    void landLosingSwampTypeBeforeResolutionGetsSmallerBoost() {
        Permanent shaman = addCreatureReady(player1, new ReefShaman());
        harness.addToBattlefield(player1, new GuulDrazOverseer());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Swamp()));
        harness.playLand(player1, 0);
        Permanent land = findPermanent(player1, "Swamp");

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(shaman),
                null, land.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "FOREST");
        harness.passBothPriorities();

        assertThat(otherCreature.getEffectivePower()).isEqualTo(3);
        assertThat(otherCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Landfall boosts creatures present at resolution, excluding opposing and later creatures")
    void recipientsAreDeterminedAtResolution() {
        harness.addToBattlefield(player1, new GuulDrazOverseer());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Swamp()));
        harness.playLand(player1, 0);
        Permanent creatureBeforeResolution = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.passBothPriorities();
        Permanent creatureAfterResolution = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(creatureBeforeResolution.getEffectivePower()).isEqualTo(4);
        assertThat(creatureAfterResolution.getEffectivePower()).isEqualTo(2);
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("A land put onto the battlefield without being played triggers landfall")
    void landEnteringWithoutBeingPlayedTriggers() {
        harness.addToBattlefield(player1, new GuulDrazOverseer());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.enterBattlefieldAndReturn(player1, new Swamp());
        harness.passBothPriorities();

        assertThat(otherCreature.getEffectivePower()).isEqualTo(4);
        assertThat(otherCreature.getEffectiveToughness()).isEqualTo(2);
    }
}
