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
}
