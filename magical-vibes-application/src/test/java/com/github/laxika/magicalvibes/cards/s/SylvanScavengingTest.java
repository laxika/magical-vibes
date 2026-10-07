package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SylvanScavenging.class, GrizzlyBears.class, AirElemental.class, GiantGrowth.class, Unsummon.class})
class SylvanScavengingTest extends BaseCardTest {

    private static final String COUNTER = "Put a +1/+1 counter on target creature you control";
    private static final String RACCOON = "Create a 3/3 green Raccoon creature token if you control a creature with power 4 or greater";

    @Test
    @DisplayName("Counter mode targets and grows a creature you control")
    void counterMode() {
        harness.addToBattlefield(player1, new SylvanScavenging());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        moveToEndStep();
        harness.handleListChoice(player1, COUNTER);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opposingCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Token mode creates a green 3/3 Raccoon when a large creature is controlled")
    void tokenMode() {
        harness.addToBattlefield(player1, new SylvanScavenging());
        harness.addToBattlefield(player1, new AirElemental());

        moveToEndStep();
        harness.handleListChoice(player1, RACCOON);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.RACCOON);
        assertThat(token.getEffectivePower()).isEqualTo(3);
        assertThat(token.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Token mode does nothing without a creature with power 4 or greater")
    void tokenModeRequiresLargeCreature() {
        harness.addToBattlefield(player1, new SylvanScavenging());
        harness.addToBattlefield(player1, new GrizzlyBears());

        moveToEndStep();
        harness.handleListChoice(player1, RACCOON);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("An opponent's large creature does not enable the token mode")
    void opposingLargeCreatureDoesNotEnableToken() {
        harness.addToBattlefield(player1, new SylvanScavenging());
        harness.addToBattlefield(player2, new AirElemental());

        moveToEndStep();
        harness.handleListChoice(player1, RACCOON);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("The token condition can become true after choosing the mode")
    void tokenConditionBecomesTrueInResponse() {
        harness.addToBattlefield(player1, new SylvanScavenging());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        moveToEndStep();
        harness.handleListChoice(player1, RACCOON);
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.ensurePriority(player1);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }

    @Test
    @DisplayName("Removing the only large creature in response prevents the token")
    void tokenConditionBecomesFalseInResponse() {
        harness.addToBattlefield(player1, new SylvanScavenging());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AirElemental());

        moveToEndStep();
        harness.handleListChoice(player1, RACCOON);
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.ensurePriority(player1);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Air Elemental");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("The counter mode does not affect a target returned to hand")
    void counterTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new SylvanScavenging());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        moveToEndStep();
        harness.handleListChoice(player1, COUNTER);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.ensurePriority(player1);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability does not trigger during the opponent's end step")
    void doesNotTriggerOnOpponentsEndStep() {
        harness.addToBattlefield(player1, new SylvanScavenging());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    private void moveToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
    }
}
