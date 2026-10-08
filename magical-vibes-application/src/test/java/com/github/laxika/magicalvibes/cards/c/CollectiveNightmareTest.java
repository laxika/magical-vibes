package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DregRecycler;
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

@CardUsed({CollectiveNightmare.class, ColossalDreadmaw.class, Forest.class, GrizzlyBears.class, DregRecycler.class})
class CollectiveNightmareTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature -3/-3 until end of turn")
    void givesTargetCreatureMinusThreeMinusThree() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new CollectiveNightmare()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new CollectiveNightmare()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(6);
        assertThat(target.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Convoke taps two creatures to help cast the spell")
    void castsWithConvoke() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        Permanent firstConvokeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondConvokeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CollectiveNightmare()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstantWithConvoke(player1, 0, List.of(target.getId()),
                List.of(firstConvokeCreature.getId(), secondConvokeCreature.getId()));

        assertThat(firstConvokeCreature.isTapped()).isTrue();
        assertThat(secondConvokeCreature.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new CollectiveNightmare()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("A creature reduced to nonpositive toughness dies")
    void reducedToughnessKillsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DregRecycler());
        harness.setHand(player1, List.of(new CollectiveNightmare()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Dreg Recycler");
        harness.assertInGraveyard(player2, "Dreg Recycler");
    }

    @Test
    @DisplayName("Three black creatures can convoke the entire spell, including its own target")
    void convokesEntireCostUsingOwnTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DregRecycler());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new DregRecycler());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new DregRecycler());
        target.setSummoningSick(true);
        second.setSummoningSick(true);
        third.setSummoningSick(true);
        harness.setHand(player1, List.of(new CollectiveNightmare()));

        harness.castInstantWithConvoke(player1, 0, List.of(target.getId()),
                List.of(target.getId(), second.getId(), third.getId()));

        assertThat(target.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        harness.assertInGraveyard(player1, "Dreg Recycler");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(second, third);
    }

    @Test
    @DisplayName("Green creatures cannot convoke the black mana requirement")
    void wrongColorCannotPayBlackMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CollectiveNightmare()));

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0, List.of(target.getId()),
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Collective Nightmare");
    }

    @Test
    @DisplayName("A tapped creature cannot convoke")
    void tappedCreatureCannotConvoke() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DregRecycler());
        Permanent convoker = harness.addToBattlefieldAndReturn(player1, new DregRecycler());
        convoker.tap();
        harness.setHand(player1, List.of(new CollectiveNightmare()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0, List.of(target.getId()),
                List.of(convoker.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        harness.assertInHand(player1, "Collective Nightmare");
    }

    @Test
    @DisplayName("The spell does not affect another creature when its target leaves before resolution")
    void targetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DregRecycler());
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new DregRecycler());
        target.setSummoningSick(false);
        harness.setHand(player1, List.of(new CollectiveNightmare()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.activateAbility(player2, 0, null, null);
        harness.handlePermanentChosen(player2, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target).contains(survivor);
        assertThat(survivor.getEffectivePower()).isEqualTo(2);
        assertThat(survivor.getEffectiveToughness()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Collective Nightmare");
    }
}
