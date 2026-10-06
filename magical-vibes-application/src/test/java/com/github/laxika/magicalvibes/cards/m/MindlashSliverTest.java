package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BenalishCavalry;
import com.github.laxika.magicalvibes.cards.b.BonesplitterSliver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MindlashSliver.class, BonesplitterSliver.class, BenalishCavalry.class})
class MindlashSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Mindlash Sliver's ability sacrifices itself and makes each player discard")
    void sacrificesItselfAndEachPlayerDiscards() {
        Permanent mindlash = addCreatureReady(player1, new MindlashSliver());
        BenalishCavalry player1Card = new BenalishCavalry();
        BenalishCavalry player2Card = new BenalishCavalry();
        harness.setHand(player1, List.of(player1Card));
        harness.setHand(player2, List.of(player2Card));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        discardFor(player1);
        discardFor(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mindlash);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(mindlash.getCard());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(player1Card);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(player2Card);
    }

    @Test
    @DisplayName("Mindlash Sliver grants the ability to another Sliver")
    void grantsAbilityToAnotherSliver() {
        harness.addToBattlefield(player1, new MindlashSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonesplitterSliver());
        harness.setHand(player1, List.of(new BenalishCavalry()));
        harness.setHand(player2, List.of(new BenalishCavalry()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        discardFor(player1);
        discardFor(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(otherSliver);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Mindlash Sliver grants the ability to an opposing Sliver")
    void grantsAbilityToOpposingSliver() {
        harness.addToBattlefield(player1, new MindlashSliver());
        Permanent opposingSliver = addCreatureReady(player2, new BonesplitterSliver());
        harness.setHand(player1, List.of(new BenalishCavalry()));
        harness.setHand(player2, List.of(new BenalishCavalry()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        discardFor(player2);
        discardFor(player1);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingSliver);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Mindlash Sliver does not grant the ability to non-Sliver creatures")
    void doesNotGrantAbilityToNonSlivers() {
        harness.addToBattlefield(player1, new MindlashSliver());
        addCreatureReady(player1, new BenalishCavalry());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Players choose privately before their cards are discarded simultaneously")
    void discardsOnlyAfterEveryPlayerHasChosen() {
        harness.addToBattlefield(player1, new MindlashSliver());
        BenalishCavalry firstCard = new BenalishCavalry();
        BonesplitterSliver secondCard = new BonesplitterSliver();
        harness.setHand(player1, List.of(firstCard, new BonesplitterSliver()));
        harness.setHand(player2, List.of(secondCard, new BenalishCavalry()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        discardFor(player1);
        boolean firstCardStillInHand = gd.playerHands.get(player1.getId()).contains(firstCard);
        boolean firstCardAlreadyPublic = gd.playerGraveyards.get(player1.getId()).contains(firstCard);
        discardFor(player2);

        assertThat(firstCardStillInHand).isTrue();
        assertThat(firstCardAlreadyPublic).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1).doesNotContain(firstCard);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1).doesNotContain(secondCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(secondCard);
    }

    @Test
    @DisplayName("An empty controller hand does not prevent the opponent from discarding")
    void skipsEmptyControllerHand() {
        harness.addToBattlefield(player1, new MindlashSliver());
        BenalishCavalry opponentCard = new BenalishCavalry();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(opponentCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        discardFor(player2);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The ability resolves with both hands empty")
    void resolvesWithBothHandsEmpty() {
        harness.addToBattlefield(player1, new MindlashSliver());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mindlash Sliver");
        harness.assertInGraveyard(player1, "Mindlash Sliver");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A tapped summoning-sick Sliver can activate the ability and pays sacrifice immediately")
    void tappedSummoningSickSliverCanActivate() {
        harness.addToBattlefield(player1, new MindlashSliver());
        Permanent mindlash = findPermanent(player1, "Mindlash Sliver");
        mindlash.setSummoningSick(true);
        mindlash.tap();
        BenalishCavalry opponentCard = new BenalishCavalry();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(opponentCard));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mindlash);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(mindlash.getCard());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        discardFor(player2);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Without mana the ability cannot be activated or sacrifice its source")
    void cannotActivateWithoutMana() {
        Permanent mindlash = addCreatureReady(player1, new MindlashSliver());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(mindlash);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(mindlash.getCard());
        assertThat(gd.stack).isEmpty();
    }
    private void discardFor(com.github.laxika.magicalvibes.model.Player player) {
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player.getId());
        harness.handleCardChosen(player, 0);
    }
}
