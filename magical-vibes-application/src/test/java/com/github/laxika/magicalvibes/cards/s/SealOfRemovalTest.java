package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SealOfRemovalTest extends BaseCardTest {

    @Test
    @DisplayName("Returns an opponent's target creature to its owner's hand")
    void returnsOpponentsCreatureToItsOwnersHand() {
        harness.addToBattlefield(player1, new SealOfRemoval());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent target = findPermanent(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Seal of Removal");
    }

    @Test
    @DisplayName("Returns your target creature to its owner's hand")
    void returnsOwnCreatureToItsOwnersHand() {
        harness.addToBattlefield(player1, new SealOfRemoval());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent target = findPermanent(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new SealOfRemoval());
        harness.addToBattlefield(player2, new Forest());

        Permanent target = findPermanent(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
