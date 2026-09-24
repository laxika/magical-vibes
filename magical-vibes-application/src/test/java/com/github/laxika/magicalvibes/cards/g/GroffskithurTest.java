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
    @DisplayName("When Groffskithur becomes blocked, it returns a named card from its graveyard to hand")
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
        harness.handleMultipleCardsChosen(player1, List.of());
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
