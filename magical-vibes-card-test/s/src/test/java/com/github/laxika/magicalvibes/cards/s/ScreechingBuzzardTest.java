package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvenSoulgazer;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScreechingBuzzard.class, GlorySeeker.class, AvenSoulgazer.class})
class ScreechingBuzzardTest extends BaseCardTest {

    @Test
    @DisplayName("When Screeching Buzzard dies, its death trigger goes on the stack")
    void deathTriggerGoesOnStack() {
        harness.addToBattlefield(player1, new ScreechingBuzzard());

        setupCombatWhereBuzzardDies();

        harness.assertInGraveyard(player1, "Screeching Buzzard");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Screeching Buzzard");
    }

    @Test
    @DisplayName("Resolving the death trigger makes each opponent discard a card")
    void eachOpponentDiscardsACard() {
        harness.addToBattlefield(player1, new ScreechingBuzzard());
        harness.setHand(player1, List.of(new GlorySeeker()));
        harness.setHand(player2, List.of(new GlorySeeker(), new GlorySeeker()));

        setupCombatWhereBuzzardDies();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Glory Seeker");
    }

    @Test
    @DisplayName("A death trigger with an empty opponent hand requires no discard")
    void emptyOpponentHand() {
        harness.addToBattlefield(player1, new ScreechingBuzzard());
        harness.setHand(player2, List.of());

        setupCombatWhereBuzzardDies();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void setupCombatWhereBuzzardDies() {
        setupCombatWhereBuzzardDies(player1, player2);
    }

    private void setupCombatWhereBuzzardDies(Player attacker, Player defender) {
        Permanent buzzardPerm = findPermanent(attacker, "Screeching Buzzard");
        buzzardPerm.setSummoningSick(false);
        buzzardPerm.setAttacking(true);

        Permanent blockerPerm = addCreatureReady(defender, new AvenSoulgazer());
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);

        resolveCombat(attacker);
    }

    @Test
    @DisplayName("An opponent with one card discards their entire hand")
    void opponentWithOneCardDiscardsIt() {
        harness.addToBattlefield(player1, new ScreechingBuzzard());
        harness.setHand(player1, List.of(new GlorySeeker()));
        harness.setHand(player2, List.of(new GlorySeeker()));

        setupCombatWhereBuzzardDies();
        resolveAllTriggers();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.DiscardChoice) {
            harness.handleCardChosen(player2, 0);
        }

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Glory Seeker");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A Buzzard controlled by the other player makes its opponent choose the discard")
    void otherControllerMakesItsOpponentDiscard() {
        harness.addToBattlefield(player2, new ScreechingBuzzard());
        harness.setHand(player2, List.of(new GlorySeeker()));
        harness.setHand(player1, List.of(new GlorySeeker(), new AvenSoulgazer()));

        setupCombatWhereBuzzardDies(player2, player1);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player1, "Aven Soulgazer");
        harness.assertInHand(player1, "Glory Seeker");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
