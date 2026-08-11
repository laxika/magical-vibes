package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MischievousSneaklingTest extends BaseCardTest {

    @Test
    void canCastDuringOpponentsTurnWithFlash() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new MischievousSneakling()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.getGameService().passPriority(harness.getGameData(), player2);
        harness.castCreature(player1, 0);

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    @Test
    void entersWithChangeling() {
        harness.addToBattlefield(player1, new MischievousSneakling());

        Permanent permanent = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();

        assertThat(permanent.hasKeyword(Keyword.CHANGELING)).isTrue();
        assertThat(permanent.hasKeyword(Keyword.FLASH)).isTrue();
    }
}
