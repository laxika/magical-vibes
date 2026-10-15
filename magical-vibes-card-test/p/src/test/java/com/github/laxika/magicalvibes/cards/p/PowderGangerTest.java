package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PowderGanger.class, LeoninScimitar.class})
class PowderGangerTest extends BaseCardTest {

    @Test
    void squadCreatesOneTokenCopyPerAdditionalPayment() {
        castPowderGanger(List.of("{2}", "{2}"));

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Powder Ganger")).hasSize(3);
    }

    @Test
    void entersTriggerCanDestroyAnArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        castPowderGanger(List.of());

        resolveAllTriggers();
        harness.handlePermanentChosen(player1, artifact.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    void entersTriggerMayDeclineItsOptionalTarget() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        castPowderGanger(List.of());

        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
    }

    private void castPowderGanger(List<String> repeatedAdditionalCosts) {
        harness.setHand(player1, List.of(new PowderGanger()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.castCreatureWithRepeatedCosts(player1, 0, repeatedAdditionalCosts);
    }

    @Test
    void squadAndArtifactDestructionAreSeparateTriggeredAbilities() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        castPowderGanger(List.of("{2}"));

        resolveAllTriggers();
        harness.handlePermanentChosen(player1, artifact.getId());

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    void castingWithoutSquadPaymentCreatesNoCopies() {
        castPowderGanger(List.of());

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Powder Ganger")).hasSize(1);
    }

    @Test
    void enteringWithoutBeingCastCreatesNoSquadCopies() {
        harness.enterBattlefieldAndReturn(player1, new PowderGanger());

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Powder Ganger")).hasSize(1);
    }
}
