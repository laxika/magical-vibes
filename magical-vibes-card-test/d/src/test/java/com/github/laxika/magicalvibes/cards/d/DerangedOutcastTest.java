package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.w.WardenOfTheWall;
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

@CardUsed({DerangedOutcast.class, DawntreaderElk.class, WardenOfTheWall.class})
class DerangedOutcastTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another Human puts two counters on the target and leaves the Outcast alive")
    void sacrificeHumanPutsTwoCountersOnTarget() {
        Permanent outcast = harness.addToBattlefieldAndReturn(player1, new DerangedOutcast());
        Permanent human = harness.addToBattlefieldAndReturn(player1, new DerangedOutcast());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk());
        prepareActivation();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, human.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(outcast).doesNotContain(human);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Deranged Outcast");
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(outcast);
    }

    @Test
    @DisplayName("Can sacrifice the Outcast itself when it is the only Human")
    void canSacrificeItselfWhenOnlyHuman() {
        harness.addToBattlefield(player1, new DerangedOutcast());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk());
        prepareActivation();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.assertInGraveyard(player1, "Deranged Outcast");
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Deranged Outcast");
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void canTargetOpponentCreature() {
        harness.addToBattlefield(player1, new DerangedOutcast());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DawntreaderElk());
        prepareActivation();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability cannot be activated without enough mana")
    void abilityRequiresMana() {
        harness.addToBattlefield(player1, new DerangedOutcast());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Deranged Outcast");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Outcast can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent outcast = harness.addToBattlefieldAndReturn(player1, new DerangedOutcast());
        outcast.tap();
        outcast.setSummoningSick(true);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk());
        prepareActivation();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Deranged Outcast");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target the Outcast and sacrifice a different Human")
    void canTargetItselfAndSacrificeAnotherHuman() {
        Permanent outcast = harness.addToBattlefieldAndReturn(player1, new DerangedOutcast());
        Permanent human = harness.addToBattlefieldAndReturn(player1, new DerangedOutcast());
        prepareActivation();

        harness.activateAbility(player1, 0, 0, null, outcast.getId());
        harness.handlePermanentChosen(player1, human.getId());
        harness.passBothPriorities();

        assertThat(outcast.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(outcast).doesNotContain(human);
    }

    @Test
    @DisplayName("Sacrificing the targeted Outcast leaves no legal target")
    void canTargetAndSacrificeItself() {
        Permanent outcast = harness.addToBattlefieldAndReturn(player1, new DerangedOutcast());
        prepareActivation();

        harness.activateAbility(player1, 0, 0, null, outcast.getId());
        harness.assertInGraveyard(player1, "Deranged Outcast");
        harness.passBothPriorities();

        assertThat(outcast.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new DerangedOutcast());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new WardenOfTheWall());
        prepareActivation();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Deranged Outcast");
        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void prepareActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}