package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RagingKavu;
import com.github.laxika.magicalvibes.cards.r.RazorfootGriffin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoOrDie.class, Forest.class, RagingKavu.class, RazorfootGriffin.class})
class DoOrDieTest extends BaseCardTest {

    @Test
    @DisplayName("Target player chooses a pile and the chosen creatures are destroyed")
    void targetPlayerChoosesPileToDestroy() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player2, new RagingKavu());
        Permanent griffin = harness.addToBattlefieldAndReturn(player2, new RazorfootGriffin());
        kavu.setRegenerationShield(1);

        castDoOrDie(player2.getId());

        PendingInteraction.MultiPermanentChoice separation =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(separation.playerId()).isEqualTo(player1.getId());
        assertThat(separation.validIds()).containsExactlyInAnyOrder(kavu.getId(), griffin.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(kavu.getId()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);

        harness.assertNotOnBattlefield(player2, "Raging Kavu");
        harness.assertInGraveyard(player2, "Raging Kavu");
        harness.assertOnBattlefield(player2, "Razorfoot Griffin");
    }

    @Test
    @DisplayName("Choosing the second pile destroys the creatures left in that pile")
    void targetPlayerChoosesSecondPile() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player2, new RagingKavu());
        Permanent griffin = harness.addToBattlefieldAndReturn(player2, new RazorfootGriffin());

        castDoOrDie(player2.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(kavu.getId()));
        harness.handleMayAbilityChosen(player2, false);

        harness.assertOnBattlefield(player2, "Raging Kavu");
        harness.assertNotOnBattlefield(player2, "Razorfoot Griffin");
        harness.assertInGraveyard(player2, "Razorfoot Griffin");
    }

    @Test
    @DisplayName("Noncreature permanents are not separated or destroyed")
    void leavesNoncreaturePermanentsAlone() {
        harness.addToBattlefield(player2, new Forest());

        castDoOrDie(player2.getId());

        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty pile is legal and destroys no creatures")
    void emptyPileCanBeChosen() {
        harness.addToBattlefield(player2, new RagingKavu());

        castDoOrDie(player2.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player2, "Raging Kavu");
        harness.assertNotInGraveyard(player2, "Raging Kavu");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castDoOrDie(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new DoOrDie()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, targetPlayerId);
        harness.passBothPriorities();
    }
}
