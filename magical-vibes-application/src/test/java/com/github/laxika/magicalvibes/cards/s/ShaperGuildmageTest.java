package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShaperGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("{W}, {T}: target creature gains first strike until end of turn")
    void grantsFirstStrike() {
        addReady(player1, new ShaperGuildmage());
        Permanent bears = addReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The granted first strike wears off at end of turn")
    void firstStrikeWearsOff() {
        addReady(player1, new ShaperGuildmage());
        Permanent bears = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("{B}, {T}: target creature gets +1/+0 until end of turn")
    void boostsTargetCreature() {
        addReady(player1, new ShaperGuildmage());
        Permanent bears = addReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The +1/+0 boost wears off at end of turn")
    void boostWearsOff() {
        addReady(player1, new ShaperGuildmage());
        Permanent bears = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
