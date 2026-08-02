package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KamiOfTwistedReflectionTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target creature you control to its owner's hand")
    void bouncesOwnCreature() {
        harness.addToBattlefield(player1, new KamiOfTwistedReflection());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent target = findPermanent(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Kami of Twisted Reflection is sacrificed as a cost")
    void sacrificedAsCost() {
        harness.addToBattlefield(player1, new KamiOfTwistedReflection());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent target = findPermanent(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Kami of Twisted Reflection");
        harness.assertInGraveyard(player1, "Kami of Twisted Reflection");
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new KamiOfTwistedReflection());
        harness.addToBattlefield(player2, new LlanowarElves());

        Permanent target = findPermanent(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
