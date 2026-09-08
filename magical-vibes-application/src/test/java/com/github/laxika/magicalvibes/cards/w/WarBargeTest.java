package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.Drowned;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WarBarge.class, Drowned.class})
class WarBargeTest extends BaseCardTest {

    @Test
    void grantsIslandwalkUntilEndOfTurn() {
        Permanent barge = addBarge();
        Permanent troll = addDrowned(player2);

        enterMain();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, indexOf(barge), 0, null, troll.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, troll, Keyword.ISLANDWALK)).isTrue();

        advanceToNextTurn(player1);

        assertThat(gqs.hasKeyword(gd, troll, Keyword.ISLANDWALK)).isFalse();
    }

    @Test
    void destroysEachTargetWhenSourceLeaves() {
        Permanent barge = addBarge();
        Permanent firstDrowned = addDrowned(player2);
        Permanent secondDrowned = addDrowned(player2);
        firstDrowned.setRegenerationShield(1);
        secondDrowned.setRegenerationShield(1);

        enterMain();
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, indexOf(barge), 0, null, firstDrowned.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(barge), 0, null, secondDrowned.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, barge));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card instanceof Drowned)
                .hasSize(2);
        harness.assertInHand(player1, "War Barge");
    }

    @Test
    void destroysTargetWhenSourceIsDestroyed() {
        Permanent barge = addBarge();
        Permanent drowned = addDrowned(player2);
        drowned.setRegenerationShield(1);

        enterMain();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, indexOf(barge), 0, null, drowned.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, barge));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(barge.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(drowned.getCard());
    }

    @Test
    void doesNotDestroyNewPermanentAfterTargetLeaves() {
        Permanent barge = addBarge();
        Permanent originalDrowned = addDrowned(player2);

        enterMain();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, indexOf(barge), 0, null, originalDrowned.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, originalDrowned));
        Permanent replacementDrowned = addDrowned(player2);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, barge));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(replacementDrowned);
        assertThat(gd.playerHands.get(player2.getId())).contains(originalDrowned.getCard());
    }

    @Test
    void delayedDestructionExpiresAtEndOfTurn() {
        Permanent barge = addBarge();
        Permanent troll = addDrowned(player2);

        enterMain();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, indexOf(barge), 0, null, troll.getId());
        harness.passBothPriorities();

        advanceToNextTurn(player1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, barge));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(troll);
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent barge = addBarge();
        Permanent otherBarge = harness.addToBattlefieldAndReturn(player2, new WarBarge());

        enterMain();
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(barge), 0, null, otherBarge.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addBarge() {
        return harness.addToBattlefieldAndReturn(player1, new WarBarge());
    }

    private Permanent addDrowned(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Drowned());
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void enterMain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        Player nextActivePlayer = currentActivePlayer.equals(player1) ? player2 : player1;
        harness.passUntil(nextActivePlayer, TurnStep.PRECOMBAT_MAIN);
    }
}
