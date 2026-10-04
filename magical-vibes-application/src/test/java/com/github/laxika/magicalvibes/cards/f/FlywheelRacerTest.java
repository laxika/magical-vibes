package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.EtchedFamiliar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlywheelRacer.class, EtchedFamiliar.class})
class FlywheelRacerTest extends BaseCardTest {

    @Test
    void cannotAddManaUnlessItIsACreature() {
        addCreatureReady(player1, new FlywheelRacer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Activate only if this permanent is a creature");
    }

    @Test
    void crewsAndThenAddsManaOfAnyColor() {
        Permanent racer = addCreatureReady(player1, new FlywheelRacer());
        Permanent creature = addCreatureReady(player1, new EtchedFamiliar());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, racer)).isTrue();
        assertThat(creature.isTapped()).isTrue();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(racer.isTapped()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void producesEachColorWithoutUsingTheStack(ManaColor color) {
        Permanent racer = addCreatureReady(player1, new FlywheelRacer());
        harness.addToBattlefield(player1, new EtchedFamiliar());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(racer.isTapped()).isTrue();
    }

    @Test
    void newlyEnteredCreatureCanCrewButNewlyEnteredRacerCannotTapForMana() {
        Permanent racer = harness.addToBattlefieldAndReturn(player1, new FlywheelRacer());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new EtchedFamiliar());
        racer.setSummoningSick(true);
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, racer)).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(racer.isTapped()).isFalse();
    }

    @Test
    void vigilanceLeavesAttackingRacerAvailableForMana() {
        Permanent racer = addCreatureReady(player1, new FlywheelRacer());
        addCreatureReady(player1, new EtchedFamiliar());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        assertThat(racer.isAttacking()).isTrue();
        assertThat(racer.isTapped()).isFalse();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(racer.isAttacking()).isTrue();
        assertThat(racer.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void crewAnimationExpiresAtEndOfTurn() {
        Permanent racer = addCreatureReady(player1, new FlywheelRacer());
        addCreatureReady(player1, new EtchedFamiliar());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, racer)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, racer)).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Activate only if this permanent is a creature");
    }
}
