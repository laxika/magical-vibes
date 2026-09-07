package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CloneLegion.class, Forest.class, GrizzlyBears.class, SerraAngel.class})
class CloneLegionTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a token copy for each creature the target player controls")
    void createsTokenCopyForEachTargetPlayersCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new SerraAngel());
        harness.addToBattlefield(player2, new Forest());

        castCloneLegion(player2);

        assertThat(tokenCount(player1)).isEqualTo(2);
        assertThat(tokenCount(player2)).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Creates no tokens when the target player controls no creatures")
    void createsNoTokensWithoutTargetCreatures() {
        harness.addToBattlefield(player2, new Forest());

        castCloneLegion(player2);

        assertThat(tokenCount(player1)).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    private void castCloneLegion(Player targetPlayer) {
        harness.setHand(player1, List.of(new CloneLegion()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.castSorcery(player1, 0, targetPlayer.getId());
        harness.passBothPriorities();
    }

    private long tokenCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .map(permanent -> permanent.getCard())
                .filter(Card::isToken)
                .count();
    }
}
