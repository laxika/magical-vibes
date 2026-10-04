package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.UginTheSpiritDragon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlamerushRider.class, GrizzlyBears.class, GiantGrowth.class, UginTheSpiritDragon.class})
class FlamerushRiderTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking alongside another creature creates a tapped and attacking copy")
    void attackCreatesTappedAttackingCopy() {
        addCreatureReady(player1, new FlamerushRider());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

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
        assertThat(copy.isAttacking()).isTrue();
        assertThat(copy.isAttackedThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The copied attacker is exiled at end of combat")
    void copyIsExiledAtEndOfCombat() {
        addCreatureReady(player1, new FlamerushRider());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

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
        addCreatureReady(player1, new FlamerushRider());

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

    @Test
    @DisplayName("The token may attack a planeswalker while the original attackers attack its controller")
    void tokenDefenderIsChosenIndependently() {
        Permanent rider = addCreatureReady(player1, new FlamerushRider());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent ugin = harness.addToBattlefieldAndReturn(player2, new UginTheSpiritDragon());
        keepCombatOpen();

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, ugin.getId());

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(rider.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(copy.getAttackTarget()).isEqualTo(ugin.getId());
    }

    @Test
    @DisplayName("Dash does not create an enters-the-battlefield triggered ability")
    void dashCreatesNoEtbTrigger() {
        harness.setHand(player1, List.of(new FlamerushRider()));
        harness.addMana(player1, ManaColor.RED, 4);
        keepCombatOpen();

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Flamerush Rider");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An attacker removed before resolution cannot be copied")
    void removedTargetIsNotCopied() {
        addCreatureReady(player1, new FlamerushRider());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        keepCombatOpen();

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        gd.playerGraveyards.get(player1.getId()).add(bears.getCard());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("A target that stops attacking before resolution cannot be copied")
    void targetMustStillBeAttackingAtResolution() {
        addCreatureReady(player1, new FlamerushRider());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        keepCombatOpen();

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, bears.getId());
        bears.setAttacking(false);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Casting normally grants no dash haste or end-step return")
    void normalCastDoesNotApplyDash() {
        harness.setHand(player1, List.of(new FlamerushRider()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent rider = findPermanent(player1, "Flamerush Rider");
        assertThat(rider.hasKeyword(Keyword.HASTE)).isFalse();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Flamerush Rider");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void keepCombatOpen() {
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
    }
}
