package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StampedeTest extends BaseCardTest {

    @Test
    @DisplayName("Stampede buffs all attacking creatures (any player) and grants trample")
    void buffsAllAttackers() {
        Permanent p1Attacker = addReadyCreature(player1, new GrizzlyBears());
        Permanent p2Attacker = addReadyCreature(player2, new GrizzlyBears());
        Permanent p1Idle = addReadyCreature(player1, new GrizzlyBears());
        p1Attacker.setAttacking(true);
        p2Attacker.setAttacking(true);

        harness.setHand(player1, List.of(new Stampede()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(p1Attacker.getEffectivePower()).isEqualTo(3);
        assertThat(p1Attacker.getEffectiveToughness()).isEqualTo(2);
        assertThat(p1Attacker.hasKeyword(Keyword.TRAMPLE)).isTrue();

        assertThat(p2Attacker.getEffectivePower()).isEqualTo(3);
        assertThat(p2Attacker.getEffectiveToughness()).isEqualTo(2);
        assertThat(p2Attacker.hasKeyword(Keyword.TRAMPLE)).isTrue();

        assertThat(p1Idle.getEffectivePower()).isEqualTo(2);
        assertThat(p1Idle.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Stampede effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.setHand(player1, List.of(new Stampede()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(3);
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(2);
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
