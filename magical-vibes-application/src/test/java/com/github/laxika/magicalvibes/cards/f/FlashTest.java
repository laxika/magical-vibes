package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlashTest extends BaseCardTest {

    private void castFlash() {
        // Flash costs {1}{U}.
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Paying the reduced cost keeps the creature on the battlefield")
    void payingReducedCostKeepsCreature() {
        harness.setHand(player1, List.of(new Flash(), new GrizzlyBears()));
        castFlash();

        // Put Grizzly Bears (index 0 now that Flash has left the hand) onto the battlefield.
        harness.handleCardChosen(player1, 0);

        // Grizzly Bears is {1}{G}; reduced by {2} the cost to keep it is {G}.
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Declining to pay sacrifices the creature")
    void decliningToPaySacrificesCreature() {
        harness.setHand(player1, List.of(new Flash(), new GrizzlyBears()));
        castFlash();

        harness.handleCardChosen(player1, 0);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Accepting without enough mana still sacrifices the creature")
    void cannotPaySacrificesCreature() {
        harness.setHand(player1, List.of(new Flash(), new GrizzlyBears()));
        castFlash();

        harness.handleCardChosen(player1, 0);
        // No green mana available — the reduced {G} cost cannot be paid.
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Declining to put a creature leaves it in hand")
    void decliningToPutLeavesCreatureInHand() {
        harness.setHand(player1, List.of(new Flash(), new GrizzlyBears()));
        castFlash();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("With no creature in hand the spell resolves with no choice")
    void noCreatureInHandResolvesHarmlessly() {
        harness.setHand(player1, List.of(new Flash()));
        castFlash();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }
}
