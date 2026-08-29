package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.m.MossbridgeTroll;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WarBarge.class, MossbridgeTroll.class, Mountain.class})
class WarBargeTest extends BaseCardTest {

    @Test
    void grantsIslandwalkUntilEndOfTurn() {
        Permanent barge = addBarge();
        Permanent troll = addTroll(player2);

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
        Permanent firstTroll = addTroll(player2);
        Permanent secondTroll = addTroll(player2);

        enterMain();
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, indexOf(barge), 0, null, firstTroll.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(barge), 0, null, secondTroll.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, barge));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card instanceof MossbridgeTroll)
                .hasSize(2);
        harness.assertInHand(player1, "War Barge");
    }

    @Test
    void delayedDestructionExpiresAtEndOfTurn() {
        Permanent barge = addBarge();
        Permanent troll = addTroll(player2);

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
        Permanent mountain = new Permanent(new Mountain());
        gd.playerBattlefields.get(player2.getId()).add(mountain);

        enterMain();
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(barge), 0, null, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addBarge() {
        Permanent barge = new Permanent(new WarBarge());
        gd.playerBattlefields.get(player1.getId()).add(barge);
        return barge;
    }

    private Permanent addTroll(Player player) {
        Permanent troll = new Permanent(new MossbridgeTroll());
        gd.playerBattlefields.get(player.getId()).add(troll);
        return troll;
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
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
