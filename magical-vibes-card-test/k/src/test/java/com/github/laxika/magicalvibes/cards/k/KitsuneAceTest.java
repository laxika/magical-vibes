package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KitsuneAce.class, DuskLegionDreadnought.class, GrizzlyBears.class})
class KitsuneAceTest extends BaseCardTest {

    @Test
    void vehicleGainsFirstStrikeWhenItAttacks() {
        addCreatureReady(player1, new KitsuneAce());
        Permanent vehicle = addCreatureReady(player1, new DuskLegionDreadnought());
        crewVehicle(vehicle);

        declareAttackers(player1, List.of(indexOf(player1, vehicle)));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.handleListChoice(player1, "That Vehicle gains first strike until end of turn");
            harness.passBothPriorities();
        });

        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void untapModeUntapsKitsuneAce() {
        Permanent ace = addCreatureReady(player1, new KitsuneAce());
        Permanent vehicle = addCreatureReady(player1, new DuskLegionDreadnought());
        crewVehicle(vehicle);

        assertThat(ace.isTapped()).isTrue();
        declareAttackers(player1, List.of(indexOf(player1, vehicle)));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Untap this creature");
        harness.passBothPriorities();

        assertThat(ace.isTapped()).isFalse();
    }

    @Test
    void doesNotTriggerForAnotherCreatureAttacking() {
        addCreatureReady(player1, new KitsuneAce());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(indexOf(player1, bears)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
    }

    private void crewVehicle(Permanent vehicle) {
        harness.activateAbility(player1, indexOf(player1, vehicle), null, null);
        harness.passBothPriorities();
    }

    @Test
    void choosesModeBeforePlayersCanRespondToTheTrigger() {
        addCreatureReady(player1, new KitsuneAce());
        Permanent vehicle = addCreatureReady(player1, new DuskLegionDreadnought());
        crewVehicle(vehicle);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(indexOf(player1, vehicle)));

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
            harness.handleListChoice(player1, "That Vehicle gains first strike until end of turn");

            assertThat(gd.stack).hasSize(1);
            assertThat(gqs.hasKeyword(gd, vehicle, Keyword.FIRST_STRIKE)).isFalse();
            harness.passBothPriorities();
            assertThat(gqs.hasKeyword(gd, vehicle, Keyword.FIRST_STRIKE)).isTrue();
        });
    }

    @Test
    void doesNotTriggerForOpponentsVehicleAttacking() {
        addCreatureReady(player1, new KitsuneAce());
        addCreatureReady(player2, new GrizzlyBears());
        Permanent vehicle = addCreatureReady(player2, new DuskLegionDreadnought());
        harness.activateAbility(player2, indexOf(player2, vehicle), null, null);
        harness.passBothPriorities();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(indexOf(player2, vehicle)));

            assertThat(gd.stack).isEmpty();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
            assertThat(gqs.hasKeyword(gd, vehicle, Keyword.FIRST_STRIKE)).isFalse();
        });
    }

    @Test
    void firstStrikeModeStillResolvesAfterAceLeavesBattlefield() {
        Permanent ace = addCreatureReady(player1, new KitsuneAce());
        Permanent vehicle = addCreatureReady(player1, new DuskLegionDreadnought());
        crewVehicle(vehicle);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(indexOf(player1, vehicle)));
            gd.playerBattlefields.get(player1.getId()).remove(ace);
            gd.playerGraveyards.get(player1.getId()).add(ace.getCard());
            harness.passBothPriorities();
            harness.handleListChoice(player1, "That Vehicle gains first strike until end of turn");
            harness.passBothPriorities();

            assertThat(gqs.hasKeyword(gd, vehicle, Keyword.FIRST_STRIKE)).isTrue();
        });
    }

    @Test
    void firstStrikeAppliesOnlyToAttackingVehicleAndExpiresAtEndOfTurn() {
        addCreatureReady(player1, new KitsuneAce());
        Permanent vehicle = addCreatureReady(player1, new DuskLegionDreadnought());
        Permanent otherVehicle = addCreatureReady(player1, new DuskLegionDreadnought());
        crewVehicle(vehicle);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(indexOf(player1, vehicle)));
            harness.passBothPriorities();
            harness.handleListChoice(player1, "That Vehicle gains first strike until end of turn");
            harness.passBothPriorities();

            assertThat(gqs.hasKeyword(gd, vehicle, Keyword.FIRST_STRIKE)).isTrue();
            assertThat(gqs.hasKeyword(gd, otherVehicle, Keyword.FIRST_STRIKE)).isFalse();
        });

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.FIRST_STRIKE)).isFalse();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
