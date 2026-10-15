package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarduSkullhunter.class})
class MarduSkullhunterTest extends BaseCardTest {

    @Test
    @DisplayName("Mardu Skullhunter enters tapped")
    void entersTapped() {
        castMarduSkullhunter();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Raid ETB makes target opponent discard a card")
    void raidMakesOpponentDiscard() {
        harness.setHand(player2, List.of(new MarduSkullhunter()));
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        castMarduSkullhunter();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Mardu Skullhunter");
    }

    @Test
    @DisplayName("Raid ETB does not trigger if you did not attack this turn")
    void raidDoesNotTriggerWithoutAttack() {
        harness.setHand(player2, List.of(new MarduSkullhunter()));
        castMarduSkullhunter();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Raid ETB target choice only offers opponents")
    void targetMustBeOpponent() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        castMarduSkullhunter();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(player2.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("Raid still triggers after the creature that attacked leaves the battlefield")
    void raidPersistsAfterAttackerDies() {
        harness.setHand(player2, List.of(new MarduSkullhunter()));
        Permanent attacker = addCreatureReady(player1, new MarduSkullhunter());
        declareAttackers(List.of(0));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        attacker.setMarkedDamage(1);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        castMarduSkullhunter();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Mardu Skullhunter");
    }

    @Test
    @DisplayName("The opponent chooses exactly one card to discard")
    void opponentChoosesOneCard() {
        MarduSkullhunter kept = new MarduSkullhunter();
        MarduSkullhunter discarded = new MarduSkullhunter();
        harness.setHand(player2, List.of(kept, discarded));
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        castMarduSkullhunter();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent with an empty hand is still a legal raid target")
    void raidResolvesAgainstEmptyHand() {
        harness.setHand(player2, List.of());
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        castMarduSkullhunter();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The opponent's attack does not enable your raid ability")
    void opponentsAttackDoesNotEnableRaid() {
        harness.setHand(player2, List.of(new MarduSkullhunter()));
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());
        castMarduSkullhunter();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Raid resolves even if Mardu Skullhunter dies in response")
    void raidResolvesAfterSourceDies() {
        harness.setHand(player2, List.of(new MarduSkullhunter()));
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        castMarduSkullhunter();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        findPermanent(player1, "Mardu Skullhunter").setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.assertNotOnBattlefield(player1, "Mardu Skullhunter");
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Mardu Skullhunter");
    }

    private void castMarduSkullhunter() {
        harness.castFromHand(player1, new MarduSkullhunter(), "{1}{B}");
    }
}
