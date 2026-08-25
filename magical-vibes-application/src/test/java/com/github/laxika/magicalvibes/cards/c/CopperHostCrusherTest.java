package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CopperHostCrusher.class, Shock.class})
class CopperHostCrusherTest extends BaseCardTest {

    @Test
    @DisplayName("Copper Host Crusher has trample and hexproof")
    void hasTrampleAndHexproof() {
        Permanent crusher = addCrusherReady(player1);

        assertThat(gqs.hasKeyword(gd, crusher, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, crusher, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Opponent cannot target Copper Host Crusher with spells")
    void opponentCannotTargetWithSpells() {
        Permanent crusher = addCrusherReady(player1);

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, crusher.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    private Permanent addCrusherReady(Player player) {
        Permanent crusher = new Permanent(new CopperHostCrusher());
        crusher.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(crusher);
        return crusher;
    }
}
