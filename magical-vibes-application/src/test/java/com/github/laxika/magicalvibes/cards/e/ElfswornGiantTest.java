package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ElfswornGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall creates a 1/1 Elf Warrior token when a land enters under your control")
    void landfallCreatesElfWarrior() {
        harness.addToBattlefield(player1, new ElfswornGiant());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent elfWarrior = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Elf Warrior"))
                .findFirst()
                .orElseThrow();
        assertThat(elfWarrior.getEffectivePower()).isEqualTo(1);
        assertThat(elfWarrior.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("A land entering under an opponent's control does not trigger landfall")
    void opponentLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new ElfswornGiant());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Elf Warrior"))
                .count()).isZero();
    }
}
