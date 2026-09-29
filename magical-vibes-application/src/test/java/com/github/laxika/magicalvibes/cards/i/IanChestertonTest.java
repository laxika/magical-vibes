package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TheTaleOfTamiyo;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IanChesterton.class, TheTaleOfTamiyo.class, GrizzlyBears.class})
class IanChestertonTest extends BaseCardTest {

    @Test
    void givesSagaSpellsReplicateAtTheirManaCost() {
        harness.addToBattlefield(player1, new IanChesterton());
        harness.setHand(player1, List.of(new TheTaleOfTamiyo()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantmentWithRepeatedCosts(player1, 0, List.of("{2}{U}"));
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
    }

    @Test
    void doesNotGiveReplicateToNonSagaSpells() {
        harness.addToBattlefield(player1, new IanChesterton());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
    }
}
