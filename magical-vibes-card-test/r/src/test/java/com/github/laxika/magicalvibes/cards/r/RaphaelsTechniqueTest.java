package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.n.NotionThief;
import com.github.laxika.magicalvibes.cards.z.ZooEscapees;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RaphaelsTechnique.class, ZooEscapees.class, NotionThief.class})
class RaphaelsTechniqueTest extends BaseCardTest {

    @Test
    @DisplayName("Each player independently may discard their hand and draw seven cards")
    void playersChooseIndependently() {
        Card player1HandCard = new ZooEscapees();
        Card player2HandCard = new ZooEscapees();
        harness.setHand(player1, List.of(new RaphaelsTechnique(), player1HandCard));
        harness.setHand(player2, List.of(player2HandCard));
        fillLibrary(player1, 10);
        fillLibrary(player2, 10);

        castTechnique();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(player1HandCard);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(player2HandCard);
    }

    @Test
    @DisplayName("All choices happen before an accepted player's hand changes")
    void choicesCompleteBeforeResolution() {
        Card player1HandCard = new ZooEscapees();
        Card player2HandCard = new ZooEscapees();
        harness.setHand(player1, List.of(new RaphaelsTechnique(), player1HandCard));
        harness.setHand(player2, List.of(player2HandCard));
        fillLibrary(player1, 10);
        fillLibrary(player2, 10);

        castTechnique();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(player1HandCard);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(10);

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(player2HandCard);
    }

    @Test
    @DisplayName("Players with empty hands may both draw seven")
    void emptyHandsMayBeDiscarded() {
        harness.setHand(player1, List.of(new RaphaelsTechnique()));
        harness.setHand(player2, List.of());
        fillLibrary(player1, 10);
        fillLibrary(player2, 10);

        castTechnique();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Both players may decline without discarding or drawing")
    void bothPlayersMayDecline() {
        Card first = new ZooEscapees();
        Card second = new ZooEscapees();
        harness.setHand(player1, List.of(new RaphaelsTechnique(), first));
        harness.setHand(player2, List.of(second));
        fillLibrary(player1, 10);
        fillLibrary(player2, 10);

        castTechnique();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(10);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(10);
    }

    @Test
    @DisplayName("The active player chooses first even when the opponent casts the spell")
    void choicesStartWithActivePlayer() {
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new RaphaelsTechnique()));
        harness.setHand(player2, List.of());

        castTechnique();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("Sneak returns the attacker as a cost and the returned card can be discarded")
    void sneakReturnsAttackerBeforeHandIsDiscarded() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new ZooEscapees());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.setHand(player1, List.of(new RaphaelsTechnique()));
        harness.setHand(player2, List.of());
        fillLibrary(player1, 10);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.castWithAlternateCost(player1, 0, List.of(attacker.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(attacker.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7).doesNotContain(attacker.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(attacker.getCard());
    }

    @Test
    @DisplayName("Sneak cannot be used outside the declare blockers step")
    void sneakRequiresDeclareBlockers() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new ZooEscapees());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.setHand(player1, List.of(new RaphaelsTechnique()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(attacker.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({NotionThief.class})
    @DisplayName("All accepted hands are discarded before any player draws")
    void stolenDrawsAreNotDiscardedByTheLaterPlayer() {
        Card originalHand = new ZooEscapees();
        harness.addToBattlefield(player2, new NotionThief());
        harness.setHand(player1, List.of(new RaphaelsTechnique()));
        harness.setHand(player2, List.of(originalHand));
        fillLibrary(player1, 10);
        fillLibrary(player2, 20);
        harness.forceActivePlayer(player1);

        castTechnique();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(14);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(originalHand);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(10);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(6);
    }

    @Test
    @DisplayName("Sneak cannot return a blocked attacker")
    void sneakRejectsBlockedAttacker() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new ZooEscapees());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new ZooEscapees());
        blocker.setBlocking(true);
        blocker.getBlockingTargetIds().add(attacker.getId());
        harness.setHand(player1, List.of(new RaphaelsTechnique()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(attacker.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.stack).isEmpty();
    }

    private void castTechnique() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveInstant(player1, 0);
    }

    private void fillLibrary(Player player, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new ZooEscapees());
        }
        harness.setLibrary(player, cards);
    }
}
