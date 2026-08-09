package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BloodCultist;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NekoTeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature damaging a creature taps it and keeps it tapped")
    void equippedCreatureDamagingCreatureTapsAndLocksIt() {
        Permanent cultist = addReady(player1, new BloodCultist());
        Permanent nekoTe = attachNekoTe(player1, cultist);
        Permanent target = addReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();

        advanceToNextTurn(player1);

        assertThat(target.isTapped()).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(nekoTe);
        advanceToNextTurn(player1);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Equipped creature damaging a player makes that player lose 1 life")
    void equippedCreatureDamagingPlayerCausesLifeLoss() {
        harness.setLife(player2, 20);
        Permanent attacker = addReady(player1, new GrizzlyBears());
        attachNekoTe(player1, attacker);
        attacker.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent attachNekoTe(Player player, Permanent host) {
        Permanent nekoTe = new Permanent(new NekoTe());
        nekoTe.setSummoningSick(false);
        nekoTe.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player.getId()).add(nekoTe);
        return nekoTe;
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
