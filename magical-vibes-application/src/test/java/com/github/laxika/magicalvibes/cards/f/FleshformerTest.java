package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Fleshformer.class, GrizzlyBears.class, HillGiant.class, FountainOfYouth.class})
class FleshformerTest extends BaseCardTest {

    private void payFullCost() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    @Test
    @DisplayName("Boosts self +2/+2, grants fear, and gives target creature -2/-2")
    void boostsSelfGrantsFearAndDebuffsTarget() {
        Permanent fleshformer = addCreatureReady(player1, new Fleshformer());
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        payFullCost();

        harness.activateAbility(player1, indexOf(fleshformer), null, hillGiant.getId());
        harness.passBothPriorities();

        assertThat(fleshformer.getEffectivePower()).isEqualTo(4);
        assertThat(fleshformer.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, fleshformer, Keyword.FEAR)).isTrue();
        assertThat(hillGiant.getEffectivePower()).isEqualTo(1);
        assertThat(hillGiant.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("-2/-2 kills a target with 2 toughness")
    void debuffKillsSmallTarget() {
        Permanent fleshformer = addCreatureReady(player1, new Fleshformer());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        payFullCost();

        harness.activateAbility(player1, indexOf(fleshformer), null, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("All effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent fleshformer = addCreatureReady(player1, new Fleshformer());
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        payFullCost();

        harness.activateAbility(player1, indexOf(fleshformer), null, hillGiant.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(fleshformer.getEffectivePower()).isEqualTo(2);
        assertThat(fleshformer.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, fleshformer, Keyword.FEAR)).isFalse();
        assertThat(hillGiant.getEffectivePower()).isEqualTo(3);
        assertThat(hillGiant.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot activate during opponent's turn")
    void cannotActivateDuringOpponentsTurn() {
        Permanent fleshformer = addCreatureReady(player1, new Fleshformer());
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        payFullCost();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(fleshformer), null, hillGiant.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    @Test
    @DisplayName("Can target itself: the boosts cancel but fear is granted")
    void canTargetItself() {
        Permanent fleshformer = addCreatureReady(player1, new Fleshformer());
        payFullCost();

        harness.activateAbility(player1, indexOf(fleshformer), null, fleshformer.getId());
        harness.passBothPriorities();

        assertThat(fleshformer.getEffectivePower()).isEqualTo(2);
        assertThat(fleshformer.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, fleshformer, Keyword.FEAR)).isTrue();
        harness.assertOnBattlefield(player1, "Fleshformer");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Fleshformer can activate during its controller's end step")
    void canActivateWhileTappedAndSummoningSickDuringEndStep() {
        Permanent fleshformer = harness.addToBattlefieldAndReturn(player1, new Fleshformer());
        fleshformer.setTapped(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Fleshformer());
        harness.forceStep(TurnStep.END_STEP);
        payFullCost();

        harness.activateAbility(player1, indexOf(fleshformer), null, target.getId());
        harness.passBothPriorities();

        assertThat(fleshformer.getEffectivePower()).isEqualTo(4);
        assertThat(fleshformer.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, fleshformer, Keyword.FEAR)).isTrue();
        assertThat(fleshformer.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Fleshformer");
    }

    @Test
    @DisplayName("Repeated activations accumulate while their targets remain legal")
    void repeatedActivationsAccumulate() {
        Permanent fleshformer = addCreatureReady(player1, new Fleshformer());
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new Fleshformer());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new Fleshformer());
        payFullCost();
        payFullCost();

        harness.activateAbility(player1, indexOf(fleshformer), null, firstTarget.getId());
        harness.activateAbility(player1, indexOf(fleshformer), null, secondTarget.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(fleshformer.getEffectivePower()).isEqualTo(6);
        assertThat(fleshformer.getEffectiveToughness()).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, fleshformer, Keyword.FEAR)).isTrue();
        harness.assertNotOnBattlefield(player2, "Fleshformer");
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("An activation with an illegal target grants neither the self boost nor fear")
    void illegalTargetPreventsAllEffects() {
        Permanent firstSource = addCreatureReady(player1, new Fleshformer());
        Permanent secondSource = addCreatureReady(player1, new Fleshformer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Fleshformer());
        payFullCost();
        payFullCost();

        harness.activateAbility(player1, indexOf(firstSource), null, target.getId());
        harness.activateAbility(player1, indexOf(secondSource), null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(firstSource.getEffectivePower()).isEqualTo(2);
        assertThat(firstSource.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, firstSource, Keyword.FEAR)).isFalse();
        assertThat(secondSource.getEffectivePower()).isEqualTo(4);
        assertThat(secondSource.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, secondSource, Keyword.FEAR)).isTrue();
        harness.assertInGraveyard(player2, "Fleshformer");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent fleshformer = addCreatureReady(player1, new Fleshformer());
        harness.addToBattlefield(player2, new HillGiant()); // legal creature target so the ability is activatable
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        payFullCost();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(fleshformer), null, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
