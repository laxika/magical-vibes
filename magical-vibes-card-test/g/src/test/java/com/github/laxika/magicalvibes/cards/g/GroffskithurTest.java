package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Groffskithur.class, AlphaMyr.class})
class GroffskithurTest extends BaseCardTest {

    @Test
    @DisplayName("When Groffskithur becomes blocked, it may return a named card from its graveyard to hand")
    void returnsNamedCardFromGraveyard() {
        Permanent attacker = addAttacker();
        Permanent blocker = addBlocker();
        Card graveyardCopy = new Groffskithur();
        harness.setGraveyard(player1, List.of(graveyardCopy));

        declareBlock(attacker, blocker);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(graveyardCopy.getId());

        harness.handleMultipleCardsChosen(player1, List.of(graveyardCopy.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Groffskithur");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The becomes-blocked ability may be declined")
    void mayDeclineReturn() {
        Permanent attacker = addAttacker();
        Permanent blocker = addBlocker();
        Card graveyardCopy = new Groffskithur();
        harness.setGraveyard(player1, List.of(graveyardCopy));

        declareBlock(attacker, blocker);
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCopy.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Groffskithur");
        harness.assertNotInHand(player1, "Groffskithur");
    }

    @Test
    @DisplayName("The becomes-blocked ability has no target without a named card in its controller's graveyard")
    void noNamedCardMeansNoChoice() {
        Permanent attacker = addAttacker();
        Permanent blocker = addBlocker();
        harness.setGraveyard(player1, List.of(new AlphaMyr()));
        harness.setGraveyard(player2, List.of(new Groffskithur()));

        declareBlock(attacker, blocker);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("When Groffskithur is unblocked, its becomes-blocked ability does not trigger")
    void doesNotTriggerWhenUnblocked() {
        addAttacker();
        Card graveyardCopy = new Groffskithur();
        harness.setGraveyard(player1, List.of(graveyardCopy));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Groffskithur");
        harness.assertNotInHand(player1, "Groffskithur");
    }

    @Test
    @DisplayName("A target must be chosen even when the controller intends to decline the return")
    void requiresTargetBeforeResolution() {
        Permanent attacker = addAttacker();
        Permanent blocker = addBlocker();
        Card graveyardCopy = new Groffskithur();
        harness.setGraveyard(player1, List.of(graveyardCopy));

        declareBlock(attacker, blocker);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validCardIds()).containsExactly(graveyardCopy.getId());
    }

    @Test
    @DisplayName("Only named cards in the controller's graveyard can be targeted")
    void filtersTargetsByNameAndGraveyard() {
        Permanent attacker = addAttacker();
        Permanent blocker = addBlocker();
        Card firstCopy = new Groffskithur();
        Card secondCopy = new Groffskithur();
        Card unrelatedCard = new AlphaMyr();
        Card opponentsCopy = new Groffskithur();
        harness.setGraveyard(player1, List.of(firstCopy, unrelatedCard, secondCopy));
        harness.setGraveyard(player2, List.of(opponentsCopy));

        declareBlock(attacker, blocker);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(firstCopy.getId(), secondCopy.getId());
        harness.handleMultipleCardsChosen(player1, List.of(secondCopy.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(secondCopy).doesNotContain(firstCopy);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstCopy, unrelatedCard).doesNotContain(secondCopy);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentsCopy);
    }

    @Test
    @DisplayName("A target that leaves the graveyard cannot be replaced by another named card")
    void doesNotRetargetWhenTargetLeavesGraveyard() {
        Permanent attacker = addAttacker();
        Permanent blocker = addBlocker();
        Card target = new Groffskithur();
        Card otherCopy = new Groffskithur();
        harness.setGraveyard(player1, List.of(target, otherCopy));

        declareBlock(attacker, blocker);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(otherCopy));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(otherCopy);
        assertThat(gd.findExiledCard(target.getId())).isNotNull();
        harness.assertNotInHand(player1, "Groffskithur");
    }

    @Test
    @DisplayName("Multiple blockers cause only one becomes-blocked trigger")
    void triggersOnceForMultipleBlockers() {
        Permanent attacker = addAttacker();
        Permanent firstBlocker = addBlocker();
        Permanent secondBlocker = addBlocker();
        Card firstCopy = new Groffskithur();
        Card secondCopy = new Groffskithur();
        harness.setGraveyard(player1, List.of(firstCopy, secondCopy));

        prepareDeclareBlockers();
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(firstBlocker), attackerIdx),
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(secondBlocker), attackerIdx)));
        harness.handleMultipleCardsChosen(player1, List.of(firstCopy.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addAttacker() {
        Permanent attacker = addCreatureReady(player1, new Groffskithur());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        return attacker;
    }

    private Permanent addBlocker() {
        return addCreatureReady(player2, new AlphaMyr());
    }

    private void declareBlock(Permanent attacker, Permanent blocker) {
        prepareDeclareBlockers();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
    }
}
