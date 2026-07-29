package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
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

class GrangerGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("{R}, {T}: deals 1 damage to a player and 1 damage to you")
    void burnsPlayerAndController() {
        addReady(player1, new GrangerGuildmage());
        harness.addMana(player1, ManaColor.RED, 1);
        int controllerLife = gd.playerLifeTotals.get(player1.getId());
        int opponentLife = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLife - 1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLife - 1);
    }

    @Test
    @DisplayName("{R}, {T}: deals 1 damage to a creature and 1 damage to you")
    void burnsCreatureAndController() {
        addReady(player1, new GrangerGuildmage());
        addReady(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.RED, 1);
        int controllerLife = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, harness.getPermanentId(player2, "Llanowar Elves"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLife - 1);
    }

    @Test
    @DisplayName("{W}, {T}: target creature gains first strike until end of turn")
    void grantsFirstStrike() {
        addReady(player1, new GrangerGuildmage());
        Permanent bears = addReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The granted first strike wears off at end of turn")
    void firstStrikeWearsOff() {
        addReady(player1, new GrangerGuildmage());
        Permanent bears = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
