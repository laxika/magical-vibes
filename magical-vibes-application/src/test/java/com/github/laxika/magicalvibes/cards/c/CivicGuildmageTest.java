package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CivicGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("{G}, {T}: target creature gets +0/+1 until end of turn")
    void boostsTargetCreature() {
        addReady(player1, new CivicGuildmage());
        Permanent bears = addReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("The +0/+1 boost wears off at end of turn")
    void boostWearsOff() {
        addReady(player1, new CivicGuildmage());
        Permanent bears = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("{U}, {T}: puts target creature you control on top of its owner's library")
    void tucksControlledCreature() {
        addReady(player1, new CivicGuildmage());
        Permanent bears = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerDecks.get(player1.getId()).get(0).getName())
                .isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("The tuck ability cannot target a creature you don't control")
    void tuckRejectsOpponentCreature() {
        addReady(player1, new CivicGuildmage());
        Permanent bears = addReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
