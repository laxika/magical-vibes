package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GargoyleCastle;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DontMove.class, GrizzlyBears.class, ZuranSpellcaster.class, GargoyleCastle.class})
class DontMoveTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all tapped creatures and leaves untapped creatures and lands alone")
    void destroysTappedCreaturesOnly() {
        Permanent tappedCreature = addCreatureReady(player1, new GrizzlyBears());
        tappedCreature.tap();
        addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GargoyleCastle());

        castDontMove();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Gargoyle Castle");
    }

    @Test
    @DisplayName("Destroys a creature that becomes tapped until the controller's next turn")
    void destroysCreatureThatBecomesTapped() {
        addReadySpellcaster(player1);

        castDontMove();

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Zuran Spellcaster");
        harness.assertInGraveyard(player1, "Zuran Spellcaster");
    }

    @Test
    @DisplayName("Does not trigger when a noncreature permanent becomes tapped")
    void doesNotTriggerForNoncreatures() {
        harness.addToBattlefield(player1, new GargoyleCastle());

        castDontMove();

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertOnBattlefield(player1, "Gargoyle Castle");
        assertThat(findPermanent(player1, "Gargoyle Castle").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Stops watching at the beginning of the controller's next turn")
    void expiresAtBeginningOfNextTurn() {
        addReadySpellcaster(player1);

        castDontMove();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UPKEEP);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Zuran Spellcaster");
    }

    private void castDontMove() {
        harness.castFromHand(player1, new DontMove(), "{3}{W}{W}");
        harness.passBothPriorities();
    }

    private Permanent addReadySpellcaster(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new ZuranSpellcaster());
    }
}
