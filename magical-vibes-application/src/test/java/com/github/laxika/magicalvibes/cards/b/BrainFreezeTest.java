package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrainFreeze.class, Forest.class})
class BrainFreezeTest extends BaseCardTest {

    @Test
    @DisplayName("Target player mills three cards")
    void targetPlayerMillsThreeCards() {
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        castBrainFreeze();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Storm creates one copy for each spell cast before Brain Freeze")
    void stormCreatesCopiesForEachPriorSpell() {
        gd.recordSpellCast(player1.getId(), new BrainFreeze());
        gd.recordSpellCast(player2.getId(), new BrainFreeze());

        castBrainFreeze();

        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
    }

    private void castBrainFreeze() {
        harness.setHand(player1, List.of(new BrainFreeze()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, player2.getId());
    }
}
