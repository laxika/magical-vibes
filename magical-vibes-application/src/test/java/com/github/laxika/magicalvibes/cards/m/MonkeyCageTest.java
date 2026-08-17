package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MonkeyCageTest extends BaseCardTest {

    private List<Permanent> monkeys() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Monkey"))
                .toList();
    }

    @Test
    @DisplayName("Creature entry sacrifices the Cage and creates one Monkey per mana value")
    void creatureEntryCreatesTokensEqualToManaValue() {
        harness.addToBattlefield(player1, new MonkeyCage());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Monkey Cage");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(monkeys()).hasSize(2);
        assertThat(monkeys()).allSatisfy(monkey -> {
            assertThat(monkey.getCard().getPower()).isEqualTo(2);
            assertThat(monkey.getCard().getToughness()).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Zero-mana creature entry sacrifices the Cage without creating tokens")
    void zeroManaCreatureCreatesNoTokens() {
        harness.addToBattlefield(player1, new MonkeyCage());
        harness.setHand(player1, List.of(new Memnite()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Monkey Cage");
        assertThat(monkeys()).isEmpty();
    }
}
