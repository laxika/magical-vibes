package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DwynensEliteTest extends BaseCardTest {

    @Test
    @DisplayName("With another Elf, the ETB creates a 1/1 green Elf Warrior token")
    void etbCreatesTokenWithAnotherElf() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new DwynensElite()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent token = findPermanent(player1, "Elf Warrior");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Without another Elf, no token is created (the Elite itself does not count)")
    void noTokenWithoutAnotherElf() {
        harness.setHand(player1, List.of(new DwynensElite()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Dwynen's Elite");
        assertThat(countPermanents(player1, "Elf Warrior")).isZero();
    }

    @Test
    @DisplayName("An opponent's Elf does not satisfy the intervening-if")
    void opponentElfDoesNotCount() {
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new DwynensElite()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elf Warrior")).isZero();
    }
}
