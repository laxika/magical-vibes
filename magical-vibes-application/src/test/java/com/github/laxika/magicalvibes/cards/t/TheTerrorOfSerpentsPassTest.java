package com.github.laxika.magicalvibes.cards.t;

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

@CardUsed({TheTerrorOfSerpentsPass.class, Shock.class})
class TheTerrorOfSerpentsPassTest extends BaseCardTest {

    @Test
    @DisplayName("The Terror of Serpent's Pass has hexproof")
    void hasHexproof() {
        Permanent terror = addTerrorReady(player1);

        assertThat(gqs.hasKeyword(gd, terror, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Opponent cannot target The Terror of Serpent's Pass with spells")
    void opponentCannotTargetWithSpells() {
        Permanent terror = addTerrorReady(player1);

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, terror.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    private Permanent addTerrorReady(Player player) {
        Permanent terror = new Permanent(new TheTerrorOfSerpentsPass());
        terror.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(terror);
        return terror;
    }
}
