package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MurkDwellersTest extends BaseCardTest {

    @Test
    @DisplayName("Unblocked attacker gets +2/+0")
    void unblockedGetsBoost() {
        Permanent dwellers = addReady(player1, new MurkDwellers());
        dwellers.setAttacking(true);
        addReady(player2, new GrizzlyBears()); // a potential blocker that declines to block

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of()); // no blocks — Murk Dwellers is unblocked
        harness.passBothPriorities();

        assertThat(dwellers.getPowerModifier()).isEqualTo(2);
        assertThat(dwellers.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("A blocked attacker does not get the boost")
    void blockedGetsNoBoost() {
        Permanent dwellers = addReady(player1, new MurkDwellers());
        dwellers.setAttacking(true);
        addReady(player2, new GrizzlyBears());

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(dwellers.getPowerModifier()).isEqualTo(0);
        assertThat(dwellers.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The +2/+0 wears off at end of turn")
    void boostWearsOff() {
        Permanent dwellers = addReady(player1, new MurkDwellers());
        dwellers.setAttacking(true);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(dwellers.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(dwellers.getPowerModifier()).isEqualTo(0);
        assertThat(dwellers.getToughnessModifier()).isEqualTo(0);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setupDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
