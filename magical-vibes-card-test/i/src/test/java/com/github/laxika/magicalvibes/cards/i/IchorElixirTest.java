package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.p.Panopticon;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarDieResult;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanarDieRoller;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@CardUsed({IchorElixir.class, Panopticon.class, Humility.class, MarchOfTheMachines.class})
class IchorElixirTest extends BaseCardTest {

    private PlanechaseService planar;
    private PlanarDieRoller die;
    private PlanarDieRoller originalDie;

    @BeforeEach
    void preparePlanechase() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        originalDie = (PlanarDieRoller) ReflectionTestUtils.getField(planar, "die");
        die = mock(PlanarDieRoller.class);
        when(die.numberOfRolls(gd, player1.getId())).thenCallRealMethod();
        ReflectionTestUtils.setField(planar, "die", die);

        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new Panopticon(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @AfterEach
    void restorePlanarDie() {
        ReflectionTestUtils.setField(planar, "die", originalDie);
    }

    @Test
    void addsTwoColorlessMana() {
        harness.addToBattlefield(player1, new IchorElixir());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    void manaAbilityTapsImmediatelyAndCannotBeActivatedAgain() {
        var elixir = harness.addToBattlefieldAndReturn(player1, new IchorElixir());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(elixir.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    void letsItsControllerChooseTheIgnoredPlanarResult() {
        harness.addToBattlefield(player1, new IchorElixir());
        when(die.roll()).thenReturn(PlanarDieResult.BLANK, PlanarDieResult.CHAOS);

        harness.inMutationScope(() -> planar.roll(gd, player1.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PlanarDieChoice.class);
        assertThat(gd.planechase.rollSequence).isZero();

        harness.handleListChoice(player1, "1: BLANK");

        assertThat(gd.planechase.lastRoll).isEqualTo(PlanarDieResult.CHAOS);
        assertThat(gd.planechase.rollSequence).isEqualTo(1);
    }

    @Test
    void onlyTheRollingPlayersElixirReplacesTheirRoll() {
        harness.addToBattlefield(player2, new IchorElixir());
        when(die.roll()).thenReturn(PlanarDieResult.BLANK);

        harness.inMutationScope(() -> planar.roll(gd, player1.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.planechase.lastRoll).isEqualTo(PlanarDieResult.BLANK);
    }

    @Test
    void ignoredChaosDoesNotTriggerThePlane() {
        harness.addToBattlefield(player1, new IchorElixir());
        when(die.roll()).thenReturn(PlanarDieResult.CHAOS, PlanarDieResult.BLANK);

        harness.inMutationScope(() -> planar.roll(gd, player1.getId()));
        harness.handleListChoice(player1, "1: CHAOS");

        assertThat(gd.planechase.lastRoll).isEqualTo(PlanarDieResult.BLANK);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void ignoredPlaneswalkerDoesNotPlaneswalk() {
        harness.addToBattlefield(player1, new IchorElixir());
        var originalPlane = gd.planechase.faceUp.getFirst();
        when(die.roll()).thenReturn(PlanarDieResult.PLANESWALKER, PlanarDieResult.BLANK);

        harness.inMutationScope(() -> planar.roll(gd, player1.getId()));
        harness.handleListChoice(player1, "1: PLANESWALKER");

        assertThat(gd.planechase.lastRoll).isEqualTo(PlanarDieResult.BLANK);
        assertThat(gd.planechase.faceUp).containsExactly(originalPlane);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void twoElixirsRollThreeDiceAndIgnoreTwo() {
        harness.addToBattlefield(player1, new IchorElixir());
        harness.addToBattlefield(player1, new IchorElixir());
        when(die.roll()).thenReturn(PlanarDieResult.CHAOS, PlanarDieResult.PLANESWALKER,
                PlanarDieResult.BLANK);

        harness.inMutationScope(() -> planar.roll(gd, player1.getId()));

        var choice = (PendingInteraction.PlanarDieChoice) gd.interaction.activeInteraction();
        assertThat(choice.rolls()).containsExactly(PlanarDieResult.CHAOS,
                PlanarDieResult.PLANESWALKER, PlanarDieResult.BLANK);
        harness.handleListChoice(player1, "1: CHAOS");
        assertThat(gd.planechase.rollSequence).isZero();
        harness.handleListChoice(player1, "1: PLANESWALKER");

        assertThat(gd.planechase.lastRoll).isEqualTo(PlanarDieResult.BLANK);
        assertThat(gd.planechase.rollSequence).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({Humility.class, MarchOfTheMachines.class})
    void elixirWithNoAbilitiesDoesNotReplacePlanarRolls() {
        var elixir = harness.addToBattlefieldAndReturn(player1, new IchorElixir());
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        harness.addToBattlefield(player1, new Humility());
        assertThat(gqs.isCreature(gd, elixir)).isTrue();
        assertThat(gqs.hasLostAllAbilities(gd, elixir)).isTrue();
        when(die.roll()).thenReturn(PlanarDieResult.BLANK, PlanarDieResult.CHAOS);

        harness.inMutationScope(() -> planar.roll(gd, player1.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.planechase.lastRoll).isEqualTo(PlanarDieResult.BLANK);
        assertThat(gd.planechase.rollSequence).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
