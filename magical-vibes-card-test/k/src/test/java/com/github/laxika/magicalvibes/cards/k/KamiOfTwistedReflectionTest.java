package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KamiOfTwistedReflection.class, WanderingOnes.class})
class KamiOfTwistedReflectionTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target creature you control to its owner's hand")
    void bouncesOwnCreature() {
        harness.addToBattlefield(player1, new KamiOfTwistedReflection());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WanderingOnes());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wandering Ones");
        harness.assertInHand(player1, "Wandering Ones");
    }

    @Test
    @DisplayName("Kami of Twisted Reflection is sacrificed as a cost")
    void sacrificedAsCost() {
        harness.addToBattlefield(player1, new KamiOfTwistedReflection());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WanderingOnes());

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Kami of Twisted Reflection");
        harness.assertInGraveyard(player1, "Kami of Twisted Reflection");
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new KamiOfTwistedReflection());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WanderingOnes());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not return the target if it leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new KamiOfTwistedReflection());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WanderingOnes());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Kami of Twisted Reflection");
        harness.assertNotInHand(player1, "Wandering Ones");
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Can target itself, but remains sacrificed when the ability resolves")
    void canTargetItself() {
        Permanent kami = harness.addToBattlefieldAndReturn(player1, new KamiOfTwistedReflection());

        harness.activateAbility(player1, 0, null, kami.getId());
        harness.assertInGraveyard(player1, "Kami of Twisted Reflection");
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Kami of Twisted Reflection");
        harness.assertInGraveyard(player1, "Kami of Twisted Reflection");
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick without paying mana")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent kami = harness.addToBattlefieldAndReturn(player1, new KamiOfTwistedReflection());
        kami.tap();
        kami.setSummoningSick(true);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WanderingOnes());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Kami of Twisted Reflection");
        harness.assertInHand(player1, "Wandering Ones");
    }

    @Test
    @DisplayName("Returns a controlled creature to its opponent owner's hand")
    void returnsStolenCreatureToOwner() {
        harness.addToBattlefield(player1, new KamiOfTwistedReflection());
        WanderingOnes stolenCard = new WanderingOnes();
        stolenCard.setOwnerId(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player1, stolenCard);
        gd.stolenCreatures.put(target.getId(), player2.getId());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wandering Ones");
        harness.assertNotInHand(player1, "Wandering Ones");
        harness.assertInHand(player2, "Wandering Ones");
    }

    @Test
    @DisplayName("Does not return a target that an opponent gains control of before resolution")
    void fizzlesIfTargetChangesController() {
        harness.addToBattlefield(player1, new KamiOfTwistedReflection());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WanderingOnes());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        gd.stolenCreatures.put(target.getId(), player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Wandering Ones");
        harness.assertNotInHand(player1, "Wandering Ones");
        harness.assertNotInHand(player2, "Wandering Ones");
        assertThat(gameLogContains("fizzles")).isTrue();
    }
}
