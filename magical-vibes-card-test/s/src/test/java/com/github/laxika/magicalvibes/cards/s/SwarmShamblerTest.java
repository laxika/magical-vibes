package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwarmShambler.class, Shock.class, GrizzlyBears.class, ProdigalPyromancer.class})
class SwarmShamblerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter")
    void entersWithCounter() {
        Permanent shambler = castShambler();

        assertThat(shambler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates an Insect when an opponent targets a creature with a +1/+1 counter")
    void createsInsectWhenOpponentTargetsCounteredCreature() {
        Permanent shambler = castShambler();
        castOpponentShock(shambler);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    @DisplayName("Does not create an Insect when the targeted creature has no +1/+1 counter")
    void doesNotCreateInsectForCreatureWithoutCounter() {
        castShambler();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castOpponentShock(bears);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Does not create an Insect when its controller casts the targeting spell")
    void doesNotCreateInsectForOwnSpell() {
        Permanent shambler = castShambler();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, shambler.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Does not create an Insect when an opponent's ability targets it")
    void doesNotCreateInsectForOpponentAbility() {
        Permanent shambler = castShambler();
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player2, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);

        harness.activateAbility(player2, 0, null, shambler.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when its ability resolves")
    void activatedAbilityPutsCounterOnItself() {
        Permanent shambler = castShambler();
        shambler.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shambler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(shambler.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creates an Insect when another controlled creature with a counter is targeted")
    void createsInsectForAnotherCounteredCreature() {
        castShambler();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castOpponentShock(bears);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    @DisplayName("Does not create an Insect for an opponent's countered creature")
    void doesNotCreateInsectForOpponentsCreature() {
        castShambler();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castOpponentShock(bears);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Removing the counter after targeting does not stop the token trigger")
    void createsInsectEvenIfCounterIsRemovedBeforeResolution() {
        castShambler();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        castOpponentShock(bears);

        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    @DisplayName("Each Swarm Shambler creates a token for the same targeting event")
    void eachShamblerTriggersForSameTarget() {
        Permanent shambler = castShambler();
        castShambler();

        castOpponentShock(shambler);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(2);
    }

    private Permanent castShambler() {
        harness.castFromHand(player1, new SwarmShambler(), "{G}");
        harness.passBothPriorities();
        return findPermanent(player1, "Swarm Shambler");
    }

    private void castOpponentShock(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, target.getId());
    }
}
