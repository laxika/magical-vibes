package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThoughtweftGambitTest extends BaseCardTest {

    @Test
    @DisplayName("Taps all creatures opponents control")
    void tapsOpponentCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        List<Permanent> p2 = gd.playerBattlefields.get(player2.getId());

        harness.setHand(player1, List.of(new ThoughtweftGambit()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(p2).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Untaps all creatures you control")
    void untapsOwnCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        List<Permanent> p1 = gd.playerBattlefields.get(player1.getId());
        p1.forEach(Permanent::tap);

        harness.setHand(player1, List.of(new ThoughtweftGambit()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(p1).noneMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Does not tap your own creatures")
    void doesNotTapOwnCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.setHand(player1, List.of(new ThoughtweftGambit()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(bear.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not untap opponent's creatures")
    void doesNotUntapOpponentCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent opponentBear = gd.playerBattlefields.get(player2.getId()).getFirst();
        opponentBear.tap();

        harness.setHand(player1, List.of(new ThoughtweftGambit()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(opponentBear.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not untap your non-creature permanents")
    void doesNotUntapNonCreatures() {
        harness.addToBattlefield(player1, new Island());
        Permanent island = gd.playerBattlefields.get(player1.getId()).getFirst();
        island.tap();

        harness.setHand(player1, List.of(new ThoughtweftGambit()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(island.isTapped()).isTrue();
    }
}
