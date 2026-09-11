package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CharixTheRagingIsle.class, GrizzlyBears.class, Island.class, LightningBolt.class})
class CharixTheRagingIsleTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's spell targeting Charix costs {2} more")
    void opponentSpellTargetingCharixCostsMore() {
        Permanent charix = addReady(player1);
        prepareOpponentCast(new LightningBolt(), ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, charix.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay targeting tax");
    }

    @Test
    @DisplayName("Charix does not tax an opponent's spell targeting another permanent")
    void spellTargetingAnotherPermanentIsNotTaxed() {
        addReady(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareOpponentCast(new LightningBolt(), ManaColor.RED, 1);

        harness.castInstant(player2, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Charix does not tax its controller's spell")
    void ownSpellTargetingCharixIsNotTaxed() {
        Permanent charix = addReady(player1);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, charix.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Charix gets +X/-X for the number of Islands you control")
    void boostsByIslandsYouControl() {
        Permanent charix = addReady(player1);
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, charix)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, charix)).isEqualTo(15);
    }

    @Test
    @DisplayName("Charix's activated ability boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent charix = addReady(player1);
        harness.addToBattlefield(player1, new Island());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, charix)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, charix)).isEqualTo(16);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, charix)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, charix)).isEqualTo(17);
    }

    private Permanent addReady(Player player) {
        Permanent permanent = new Permanent(new CharixTheRagingIsle());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void prepareOpponentCast(com.github.laxika.magicalvibes.model.Card spell,
                                     ManaColor color, int amount) {
        harness.forceActivePlayer(player2);
        harness.forceStep(gd.currentStep);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, color, amount);
    }
}
