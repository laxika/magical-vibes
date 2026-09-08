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

class SwarmGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("First ability boosts and grants menace to creatures you control only")
    void boostsOwnCreaturesAndGrantsMenace() {
        Permanent source = addReady(player1, new SwarmGuildmage());
        Permanent ownBears = addReady(player1, new GrizzlyBears());
        Permanent opponentBears = addReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, source, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.MENACE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("First ability wears off at end of turn")
    void firstAbilityWearsOffAtEndOfTurn() {
        Permanent source = addReady(player1, new SwarmGuildmage());
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, source, Keyword.MENACE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, source, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Second ability gains 2 life")
    void gainsTwoLife() {
        addReady(player1, new SwarmGuildmage());
        int lifeBefore = gd.getLife(player1.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
