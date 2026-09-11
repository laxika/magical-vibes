package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BoggartBrute;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeafloorStalker.class, BoggartBrute.class, FugitiveWizard.class, GrizzlyBears.class, SoulWarden.class})
class SeafloorStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("A full party reduces the ability's cost by four generic mana")
    void fullPartyReducesActivationCost() {
        Permanent stalker = addCreatureReady(player1, new SeafloorStalker());
        harness.addToBattlefield(player1, new SoulWarden());
        harness.addToBattlefield(player1, new BoggartBrute());
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(stalker), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gqs.getEffectivePower(gd, stalker)).isEqualTo(3);
    }

    @Test
    @DisplayName("The ability boosts Seafloor Stalker and makes it unblockable until cleanup")
    void boostsAndMakesUnblockableUntilCleanup() {
        Permanent stalker = addCreatureReady(player1, new SeafloorStalker());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(stalker), 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, stalker)).isEqualTo(3);
        assertThat(gqs.hasCantBeBlocked(gd, stalker)).isTrue();

        stalker.setAttacking(true);
        prepareDeclareBlockers();
        assertThatThrownBy(() -> declareBlock(blocker, stalker))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");

        gs.declareBlockers(gd, player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, stalker)).isEqualTo(2);
        assertThat(gqs.hasCantBeBlocked(gd, stalker)).isFalse();
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }
}
