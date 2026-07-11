package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
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

class ValorousChargeTest extends BaseCardTest {

    @Test
    @DisplayName("White creatures get +2/+0, non-white creatures are unaffected")
    void boostsOnlyWhiteCreatures() {
        Permanent whiteCreature = addReadyCreature(player1, new EliteVanguard()); // 2/1 White
        Permanent greenCreature = addReadyCreature(player1, new GrizzlyBears());  // 2/2 Green

        castValorousCharge();

        assertThat(whiteCreature.getEffectivePower()).isEqualTo(4);
        assertThat(whiteCreature.getEffectiveToughness()).isEqualTo(1);

        assertThat(greenCreature.getEffectivePower()).isEqualTo(2);
        assertThat(greenCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's white creatures also get +2/+0")
    void boostsAllPlayersWhiteCreatures() {
        Permanent ownWhite = addReadyCreature(player1, new EliteVanguard());
        Permanent opponentWhite = addReadyCreature(player2, new EliteVanguard());

        castValorousCharge();

        assertThat(ownWhite.getEffectivePower()).isEqualTo(4);
        assertThat(opponentWhite.getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent whiteCreature = addReadyCreature(player1, new EliteVanguard());

        castValorousCharge();

        assertThat(whiteCreature.getEffectivePower()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(whiteCreature.getEffectivePower()).isEqualTo(2);
        assertThat(whiteCreature.getEffectiveToughness()).isEqualTo(1);
    }

    private void castValorousCharge() {
        harness.setHand(player1, List.of(new ValorousCharge()));
        harness.addMana(player1, ManaColor.WHITE, 3);
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
