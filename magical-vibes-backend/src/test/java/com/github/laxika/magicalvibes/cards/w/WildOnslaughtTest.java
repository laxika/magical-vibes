package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WildOnslaughtTest extends BaseCardTest {

    // ===== Without kicker =====

    @Test
    @DisplayName("Without kicker — puts one +1/+1 counter on each creature you control")
    void withoutKickerPutsOneCounter() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WildOnslaught()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<Permanent> bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .toList();

        assertThat(bears).hasSize(2);
        for (Permanent bear : bears) {
            assertThat(bear.getPlusOnePlusOneCounters()).isEqualTo(1);
        }
    }

    // ===== With kicker =====

    @Test
    @DisplayName("With kicker — puts two +1/+1 counters on each creature you control instead")
    void withKickerPutsTwoCounters() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WildOnslaught()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castKickedInstant(player1, 0);
        harness.passBothPriorities();

        List<Permanent> bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .toList();

        assertThat(bears).hasSize(2);
        for (Permanent bear : bears) {
            assertThat(bear.getPlusOnePlusOneCounters()).isEqualTo(2);
        }
    }

    // ===== Does not affect opponent's creatures =====

    @Test
    @DisplayName("Does not put counters on opponent's creatures")
    void doesNotAffectOpponentCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WildOnslaught()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        Permanent ownBear = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        Permanent opponentBear = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(ownBear.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(opponentBear.getPlusOnePlusOneCounters()).isEqualTo(0);
    }

    // ===== No creatures =====

    @Test
    @DisplayName("Resolves without error when no creatures are on the battlefield")
    void resolvesWithNoCreatures() {
        harness.setHand(player1, List.of(new WildOnslaught()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }
}
