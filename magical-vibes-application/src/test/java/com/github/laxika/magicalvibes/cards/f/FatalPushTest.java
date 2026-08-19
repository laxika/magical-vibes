package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrayOgre;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FatalPushTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature with mana value 2 or less without revolt")
    void destroysSmallCreatureWithoutRevolt() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        castFatalPush(targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not destroy a larger creature without revolt")
    void doesNotDestroyLargerCreatureWithoutRevolt() {
        harness.addToBattlefield(player2, new GrayOgre());
        UUID targetId = harness.getPermanentId(player2, "Gray Ogre");

        castFatalPush(targetId);

        harness.assertOnBattlefield(player2, "Gray Ogre");
    }

    @Test
    @DisplayName("Revolt destroys a creature with mana value 4 or less")
    void revoltDestroysCreatureWithManaValueFourOrLess() {
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player2, new HillGiant());
        UUID targetId = harness.getPermanentId(player2, "Hill Giant");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        castFatalPush(targetId);

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Revolt does not destroy a creature with mana value greater than 4")
    void revoltDoesNotDestroyCreatureWithManaValueGreaterThanFour() {
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player2, new AirElemental());
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        castFatalPush(targetId);

        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new Forest());
        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.setHand(player1, List.of(new FatalPush()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castFatalPush(UUID targetId) {
        harness.setHand(player1, List.of(new FatalPush()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
