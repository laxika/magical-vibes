package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlamerushRiderTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking alongside another creature creates a tapped and attacking copy")
    void attackCreatesTappedAttackingCopy() {
        Permanent rider = addReadyCreature(player1, new FlamerushRider());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());

        keepCombatOpen();
        declareAttackers(player1, List.of(0, 1));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        List<Permanent> copies = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(copies).hasSize(1);
        Permanent copy = copies.getFirst();
        assertThat(copy.isTapped()).isTrue();
        assertThat(copy.isAttackedThisTurn()).isTrue();
        assertThat(copy.getAttackTarget()).isEqualTo(rider.getAttackTarget());
    }

    @Test
    @DisplayName("The copied attacker is exiled at end of combat")
    void copyIsExiledAtEndOfCombat() {
        addReadyCreature(player1, new FlamerushRider());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());

        keepCombatOpen();
        declareAttackers(player1, List.of(0, 1));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(copy.getId(), DelayedPermanentActionKind.EXILE_TOKEN_AT_END_OF_COMBAT));

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(copy);
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class)).isEmpty();
    }

    @Test
    @DisplayName("The attack trigger has no legal target when attacking alone")
    void attackingAloneDoesNotTrigger() {
        addReadyCreature(player1, new FlamerushRider());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Dash grants haste and returns Flamerush Rider to hand at end step")
    void dashGrantsHasteAndReturnsAtEndStep() {
        harness.setHand(player1, List.of(new FlamerushRider()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent rider = findPermanent(player1, "Flamerush Rider");
        assertThat(rider.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Flamerush Rider");
        harness.assertNotOnBattlefield(player1, "Flamerush Rider");
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void keepCombatOpen() {
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
    }
}
