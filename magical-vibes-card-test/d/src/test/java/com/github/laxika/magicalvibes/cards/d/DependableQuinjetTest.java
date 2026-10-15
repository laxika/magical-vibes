package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.h.HydraulicHelper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DependableQuinjet.class, HydraulicHelper.class})
class DependableQuinjetTest extends BaseCardTest {

    @Test
    void isNotACreatureBeforeCrewing() {
        Permanent quinjet = addCreatureReady(player1, new DependableQuinjet());

        assertThat(gqs.isCreature(gd, quinjet)).isFalse();
    }

    @Test
    void manaAbilityAddsOneManaOfAChosenColor() {
        addCreatureReady(player1, new DependableQuinjet());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void crewWithEnoughPowerAnimatesQuinjetAndTapsCrew() {
        Permanent quinjet = addCreatureReady(player1, new DependableQuinjet());
        Permanent firstCrew = addCreatureReady(player1, new HydraulicHelper());
        Permanent secondCrew = addCreatureReady(player1, new HydraulicHelper());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, quinjet)).isTrue();
        assertThat(firstCrew.isTapped()).isTrue();
        assertThat(secondCrew.isTapped()).isTrue();
    }

    @Test
    void cannotCrewWithoutEnoughPower() {
        addCreatureReady(player1, new DependableQuinjet());
        addCreatureReady(player1, new HydraulicHelper());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    void crewAnimationResetsAtEndOfTurn() {
        Permanent quinjet = addCreatureReady(player1, new DependableQuinjet());
        addCreatureReady(player1, new HydraulicHelper());
        addCreatureReady(player1, new HydraulicHelper());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, quinjet)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, quinjet)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void manaAbilitySupportsEveryColorAndCannotBeUsedTwiceWithoutUntapping(ManaColor color) {
        Permanent quinjet = addCreatureReady(player1, new DependableQuinjet());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(quinjet.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    void crewedQuinjetCannotBeBlockedByACreatureWithoutFlyingOrReach() {
        Permanent quinjet = addCreatureReady(player1, new DependableQuinjet());
        addCreatureReady(player1, new HydraulicHelper());
        addCreatureReady(player1, new HydraulicHelper());
        Permanent blocker = addCreatureReady(player2, new HydraulicHelper());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(bls.canBlockAttacker(gd, blocker, quinjet,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    void newlyEnteredNoncreatureQuinjetCanProduceMana() {
        Permanent quinjet = harness.addToBattlefieldAndReturn(player1, new DependableQuinjet());
        quinjet.setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(quinjet.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void summoningSickCreaturesCanCrewATappedQuinjet() {
        Permanent quinjet = addCreatureReady(player1, new DependableQuinjet());
        quinjet.tap();
        Permanent firstCrew = harness.addToBattlefieldAndReturn(player1, new HydraulicHelper());
        Permanent secondCrew = harness.addToBattlefieldAndReturn(player1, new HydraulicHelper());
        firstCrew.setSummoningSick(true);
        secondCrew.setSummoningSick(true);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gqs.isCreature(gd, quinjet)).isFalse();
        assertThat(firstCrew.isTapped()).isTrue();
        assertThat(secondCrew.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, quinjet)).isTrue();
        assertThat(quinjet.isTapped()).isTrue();
    }

    @Test
    void cannotUseOpponentsCreaturesOrTappedCreaturesToCrew() {
        addCreatureReady(player1, new DependableQuinjet());
        addCreatureReady(player1, new HydraulicHelper());
        Permanent tappedCrew = addCreatureReady(player1, new HydraulicHelper());
        tappedCrew.tap();
        addCreatureReady(player2, new HydraulicHelper());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    void newlyEnteredCrewedQuinjetCannotPayTapManaCost() {
        Permanent quinjet = harness.addToBattlefieldAndReturn(player1, new DependableQuinjet());
        quinjet.setSummoningSick(true);
        addCreatureReady(player1, new HydraulicHelper());
        addCreatureReady(player1, new HydraulicHelper());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(quinjet.isTapped()).isFalse();
    }
}
