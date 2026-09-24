package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SoulOfMigration.class)
class SoulOfMigrationTest extends BaseCardTest {

    @Test
    @DisplayName("Hardcast: creates two Bird tokens and Soul of Migration stays on the battlefield")
    void hardcastCreatesBirdsAndStays() {
        harness.setHand(player1, List.of(new SoulOfMigration()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Bird")).hasSize(2);
        harness.assertOnBattlefield(player1, "Soul of Migration");
    }

    @Test
    @DisplayName("Evoke: creates two Bird tokens, then sacrifices Soul of Migration")
    void evokeCreatesBirdsAndSacrificesSelf() {
        harness.setHand(player1, List.of(new SoulOfMigration()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithEvoke(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Bird")).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Soul of Migration");
        harness.assertInGraveyard(player1, "Soul of Migration");
    }
}
