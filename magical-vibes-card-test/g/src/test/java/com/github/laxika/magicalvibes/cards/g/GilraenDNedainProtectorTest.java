package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GilraenDNedainProtector.class, GrizzlyBears.class})
class GilraenDNedainProtectorTest extends BaseCardTest {

    @Test
    @DisplayName("Ability may immediately return another creature under its owner's control")
    void mayReturnCreatureImmediately() {
        addCreatureReady(player1, new GilraenDNedainProtector());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        activate(target);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(target.getId());
        assertThat(returned.getCounterCount(CounterType.VIGILANCE)).isZero();
        assertThat(returned.getCounterCount(CounterType.LIFELINK)).isZero();
    }

    @Test
    @DisplayName("Declining immediate return schedules the creature with vigilance and lifelink counters")
    void returnsAtNextEndStepWithKeywordCounters() {
        addCreatureReady(player1, new GilraenDNedainProtector());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        activate(target);

        harness.handleMayAbilityChosen(player1, false);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");

        advanceToEndStep();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getCounterCount(CounterType.VIGILANCE)).isEqualTo(1);
        assertThat(returned.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target Gilraen itself")
    void cannotTargetSelf() {
        Permanent gilraen = addCreatureReady(player1, new GilraenDNedainProtector());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, gilraen.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The target is already exiled when its controller chooses whether to return it")
    void exilesBeforeReturnChoice() {
        addCreatureReady(player1, new GilraenDNedainProtector());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        activate(target);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(target.getCard().getId())).isNotNull();
    }

    @Test
    void cannotTargetOpponentsCreature() {
        addCreatureReady(player1, new GilraenDNedainProtector());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void immediateReturnGoesToOwnerAndRemovesOldCounters() {
        addCreatureReady(player1, new GilraenDNedainProtector());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        gd.stolenCreatures.put(target.getId(), player2.getId());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        activate(target);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        Permanent returned = findPermanent(player2, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(target.getId());
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(returned.getCounterCount(CounterType.VIGILANCE)).isZero();
        assertThat(returned.getCounterCount(CounterType.LIFELINK)).isZero();
    }

    @Test
    void delayedReturnGoesToOwnerEvenAfterGilraenLeaves() {
        Permanent gilraen = addCreatureReady(player1, new GilraenDNedainProtector());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        gd.stolenCreatures.put(target.getId(), player2.getId());
        activate(target);
        harness.handleMayAbilityChosen(player1, false);
        gd.playerBattlefields.get(player1.getId()).remove(gilraen);
        gd.playerGraveyards.get(player1.getId()).add(gilraen.getCard());

        advanceToEndStep();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        Permanent returned = findPermanent(player2, "Grizzly Bears");
        assertThat(returned.getCounterCount(CounterType.VIGILANCE)).isEqualTo(1);
        assertThat(returned.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
    }

    @Test
    void cardRemovedFromExileDoesNotReturn() {
        addCreatureReady(player1, new GilraenDNedainProtector());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        activate(target);
        harness.handleMayAbilityChosen(player1, false);
        gd.removeFromExile(target.getCard().getId());
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());

        advanceToEndStep();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target.getCard());
    }

    @Test
    void activatingDuringEndStepWaitsForFollowingEndStep() {
        addCreatureReady(player1, new GilraenDNedainProtector());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.forceStep(TurnStep.END_STEP);
        activate(target);
        harness.handleMayAbilityChosen(player1, false);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getCounterCount(CounterType.VIGILANCE)).isEqualTo(1);
        assertThat(returned.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
    }

    @Test
    void cannotActivateWithoutTwoMana() {
        addCreatureReady(player1, new GilraenDNedainProtector());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent gilraen = addCreatureReady(player1, new GilraenDNedainProtector());
        gilraen.tap();
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent gilraen = addCreatureReady(player1, new GilraenDNedainProtector());
        gilraen.setSummoningSick(true);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void losingControlOfTargetBeforeResolutionPreventsExile() {
        addCreatureReady(player1, new GilraenDNedainProtector());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        gd.stolenCreatures.put(target.getId(), player1.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Grizzly Bears").getId()).isEqualTo(target.getId());
        assertThat(gd.findExiledCard(target.getCard().getId())).isNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void activate(Permanent target) {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
    }
}
