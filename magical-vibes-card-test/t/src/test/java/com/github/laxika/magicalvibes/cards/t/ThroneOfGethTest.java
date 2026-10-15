package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({ThroneOfGeth.class, GrizzlyBears.class, Spellbook.class})
class ThroneOfGethTest extends BaseCardTest {


    @Test
    @DisplayName("Can sacrifice itself as the only artifact, putting ability on stack")
    void canSacrificeItself() {
        addReadyThrone(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);

        // Throne auto-sacrificed itself (the only artifact)
        harness.assertNotOnBattlefield(player1, "Throne of Geth");
        harness.assertInGraveyard(player1, "Throne of Geth");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("With multiple artifacts, prompts to choose which to sacrifice")
    void promptsChoiceWithMultipleArtifacts() {
        addReadyThrone(player1);
        harness.addToBattlefield(player1, new Spellbook());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing an artifact to sacrifice puts ability on stack")
    void choosingArtifactPutsAbilityOnStack() {
        addReadyThrone(player1);
        harness.addToBattlefield(player1, new Spellbook());
        UUID spellbookId = harness.getPermanentId(player1, "Spellbook");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, spellbookId);

        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Spellbook");
        // Throne should still be on battlefield (chose to sacrifice Spellbook instead)
        harness.assertOnBattlefield(player1, "Throne of Geth");
    }


    @Test
    @DisplayName("Proliferate adds counters to chosen permanents after sacrifice")
    void proliferateAddsCountersAfterSacrifice() {
        addReadyThrone(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve ability

        // Choose to proliferate the bears
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Proliferate adds -1/-1 counter to chosen creature")
    void proliferateAddsMinusCounters() {
        addReadyThrone(player1);
        harness.addToBattlefield(player1, new Spellbook()); // extra artifact so throne isn't sacrificed
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID spellbookId = harness.getPermanentId(player1, "Spellbook");
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, spellbookId); // sacrifice Spellbook
        harness.passBothPriorities(); // resolve ability

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Proliferate can choose no permanents")
    void proliferateCanChooseNone() {
        addReadyThrone(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }


    @Test
    @DisplayName("Cannot activate when tapped")
    void cannotActivateWhenTapped() {
        Permanent throne = addReadyThrone(player1);
        throne.tap();
        harness.addToBattlefield(player1, new Spellbook());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void proliferatesPlayersAndEveryCounterKindOnChosenPermanent() {
        Permanent throne = addReadyThrone(player1);
        throne.setCounterCount(CounterType.CHARGE, 2);
        throne.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addToBattlefield(player1, new Spellbook());
        Permanent unchosen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        unchosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        gd.playerPoisonCounters.put(player1.getId(), 1);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        gd.playerEnergyCounters.put(player2.getId(), 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Spellbook"));
        assertThat(throne.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(throne.getId(), player2.getId()));

        assertThat(throne.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(throne.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(throne.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(unchosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(4);
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void selfSacrificeResolvesWhenOnlyPlayerHasCounters() {
        addReadyThrone(player1);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
        harness.assertInGraveyard(player1, "Throne of Geth");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void selfSacrificeResolvesWithoutCountersToProliferate() {
        addReadyThrone(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Throne of Geth");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addReadyThrone(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ThroneOfGeth());
        perm.setSummoningSick(false);
        return perm;
    }

}
