package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScreechingBuzzard.class, GrizzlyBears.class})
class ScreechingBuzzardTest extends BaseCardTest {

    @Test
    @DisplayName("When Screeching Buzzard dies, its death trigger goes on the stack")
    void deathTriggerGoesOnStack() {
        harness.addToBattlefield(player1, new ScreechingBuzzard());

        setupCombatWhereBuzzardDies();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Screeching Buzzard");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Screeching Buzzard");
    }

    @Test
    @DisplayName("Resolving the death trigger makes each opponent discard a card")
    void eachOpponentDiscardsACard() {
        harness.addToBattlefield(player1, new ScreechingBuzzard());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));

        setupCombatWhereBuzzardDies();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A death trigger with an empty opponent hand requires no discard")
    void emptyOpponentHand() {
        harness.addToBattlefield(player1, new ScreechingBuzzard());
        harness.setHand(player2, new ArrayList<>());

        setupCombatWhereBuzzardDies();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void setupCombatWhereBuzzardDies() {
        Permanent buzzardPerm = findPermanent(player1, "Screeching Buzzard");
        buzzardPerm.setSummoningSick(false);
        buzzardPerm.setAttacking(true);

        GrizzlyBears bigBear = new GrizzlyBears();
        bigBear.setPower(5);
        bigBear.setToughness(5);
        Permanent blockerPerm = new Permanent(bigBear);
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }
}
