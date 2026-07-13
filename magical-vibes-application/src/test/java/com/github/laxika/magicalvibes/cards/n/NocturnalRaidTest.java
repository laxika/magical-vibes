package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NocturnalRaidTest extends BaseCardTest {

    @Test
    @DisplayName("Black creatures get +2/+0, non-black creatures are unaffected")
    void boostsOnlyBlackCreatures() {
        Permanent blackCreature = addReadyCreature(player1, new BlackKnight());   // 2/2 Black
        Permanent greenCreature = addReadyCreature(player1, new GrizzlyBears());  // 2/2 Green

        castNocturnalRaid();

        assertThat(blackCreature.getEffectivePower()).isEqualTo(4);
        assertThat(blackCreature.getEffectiveToughness()).isEqualTo(2);

        assertThat(greenCreature.getEffectivePower()).isEqualTo(2);
        assertThat(greenCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's black creatures also get +2/+0")
    void boostsAllPlayersBlackCreatures() {
        Permanent ownBlack = addReadyCreature(player1, new BlackKnight());
        Permanent opponentBlack = addReadyCreature(player2, new BlackKnight());

        castNocturnalRaid();

        assertThat(ownBlack.getEffectivePower()).isEqualTo(4);
        assertThat(opponentBlack.getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent blackCreature = addReadyCreature(player1, new BlackKnight());

        castNocturnalRaid();

        assertThat(blackCreature.getEffectivePower()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blackCreature.getEffectivePower()).isEqualTo(2);
        assertThat(blackCreature.getEffectiveToughness()).isEqualTo(2);
    }

    private void castNocturnalRaid() {
        harness.setHand(player1, List.of(new NocturnalRaid()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castAndResolveInstant(player1, 0);
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
