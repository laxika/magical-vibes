package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TimelyReinforcementsTest extends BaseCardTest {

    private long soldierTokenCount() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.SOLDIER))
                .count();
    }

    private void cast() {
        harness.setHand(player1, List.of(new TimelyReinforcements()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Behind on both life and creatures: gains 6 life and creates three Soldiers")
    void bothHalvesApply() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast();

        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
        assertThat(soldierTokenCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Equal life and equal creature counts: neither half applies")
    void neitherHalfApplies() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(soldierTokenCount()).isZero();
    }

    @Test
    @DisplayName("Behind on life only: gains 6 life but creates no tokens")
    void lifeHalfOnly() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());

        cast();

        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
        assertThat(soldierTokenCount()).isZero();
    }

    @Test
    @DisplayName("Behind on creatures only: creates three Soldiers but gains no life")
    void tokenHalfOnly() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(soldierTokenCount()).isEqualTo(3);
    }
}
