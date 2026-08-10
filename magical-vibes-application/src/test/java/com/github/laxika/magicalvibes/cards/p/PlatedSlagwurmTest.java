package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlatedSlagwurmTest extends BaseCardTest {

    @Test
    @DisplayName("Plated Slagwurm has hexproof on the battlefield")
    void hasHexproof() {
        Permanent slagwurm = addSlagwurmReady(player1);

        assertThat(gqs.hasKeyword(gd, slagwurm, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Opponent cannot target Plated Slagwurm with spells")
    void opponentCannotTargetWithSpells() {
        Permanent slagwurm = addSlagwurmReady(player1);

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, slagwurm.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    private Permanent addSlagwurmReady(Player player) {
        Permanent slagwurm = new Permanent(new PlatedSlagwurm());
        slagwurm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(slagwurm);
        return slagwurm;
    }
}
