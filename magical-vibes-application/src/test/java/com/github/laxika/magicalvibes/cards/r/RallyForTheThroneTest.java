package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RallyForTheThrone.class, GrizzlyBears.class})
class RallyForTheThroneTest extends BaseCardTest {

    @Test
    @DisplayName("Creates two Human tokens without adamant")
    void createsTokensWithoutAdamant() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new RallyForTheThrone()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(humanTokenCount(player1)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Adamant gains one life for each creature after creating the tokens")
    void adamantGainsLifeForEachCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new RallyForTheThrone()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(humanTokenCount(player1)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    private long humanTokenCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Human"))
                .count();
    }
}
