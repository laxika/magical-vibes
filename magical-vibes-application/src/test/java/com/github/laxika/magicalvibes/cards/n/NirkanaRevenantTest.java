package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NirkanaRevenantTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping your Swamp adds an additional black mana")
    void ownSwampProducesExtraBlack() {
        harness.addToBattlefield(player1, new NirkanaRevenant());
        harness.addToBattlefield(player1, new Swamp());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's Swamp does not produce Nirkana Revenant's additional mana")
    void opponentSwampDoesNotProduceExtraBlack() {
        harness.addToBattlefield(player1, new NirkanaRevenant());
        harness.addToBattlefield(player2, new Swamp());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating the ability gives +1/+1 until end of turn")
    void activatingBoostsUntilEndOfTurn() {
        Permanent revenant = addRevenantReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(revenant.getEffectivePower()).isEqualTo(6);
        assertThat(revenant.getEffectiveToughness()).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(revenant.getEffectivePower()).isEqualTo(4);
        assertThat(revenant.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Tapping a non-Swamp land does not trigger the additional mana")
    void nonSwampDoesNotTrigger() {
        harness.addToBattlefield(player1, new NirkanaRevenant());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    private Permanent addRevenantReady(Player player) {
        Permanent permanent = new Permanent(new NirkanaRevenant());
        permanent.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
