package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AthreosGodOfPassageTest extends BaseCardTest {

    @Test
    @DisplayName("Athreos is not a creature below seven devotion to white and black")
    void isNotCreatureBelowDevotionThreshold() {
        Permanent athreos = harness.addToBattlefieldAndReturn(player1, new AthreosGodOfPassage());
        addBlackPermanents(4);

        assertThat(gqs.isCreature(gd, athreos)).isFalse();
    }

    @Test
    @DisplayName("Athreos becomes a creature at seven devotion to white and black")
    void becomesCreatureAtDevotionThreshold() {
        Permanent athreos = harness.addToBattlefieldAndReturn(player1, new AthreosGodOfPassage());
        addBlackPermanents(5);

        assertThat(gqs.isCreature(gd, athreos)).isTrue();
    }

    @Test
    @DisplayName("The target opponent may pay 3 life to keep an owned dead creature in the graveyard")
    void targetOpponentMayPayLife() {
        harness.addToBattlefield(player1, new AthreosGodOfPassage());
        harness.setLife(player2, 20);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        destroy(bears);
        chooseOpponentTarget();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("The dead creature returns to its owner's hand when the opponent declines")
    void decliningPaymentReturnsCreatureToOwnerHand() {
        harness.addToBattlefield(player1, new AthreosGodOfPassage());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        destroy(bears);
        chooseOpponentTarget();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Athreos does not trigger for a creature owned by an opponent")
    void doesNotTriggerForOpponentOwnedCreature() {
        harness.addToBattlefield(player1, new AthreosGodOfPassage());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        destroy(bears);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private void chooseOpponentTarget() {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    private void destroy(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();
    }

    private void addBlackPermanents(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new WalkingCorpse());
        }
    }
}
