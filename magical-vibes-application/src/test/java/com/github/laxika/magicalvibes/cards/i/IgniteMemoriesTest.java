package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(IgniteMemories.class)
class IgniteMemoriesTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the mana value of a random card in the target player's hand")
    void dealsDamageEqualToRevealedCardsManaValue() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new IgniteMemories()));
        harness.setHand(player2, List.of(new IgniteMemories()));
        addIgniteMemoriesMana();

        harness.castSorcery(player1, 0, player2.getId());
        resolveSpellAndStorm();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Storm copies Ignite Memories for each spell cast before it")
    void stormCopiesSpellForEachPriorSpell() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new IgniteMemories()));
        harness.setHand(player2, List.of(new IgniteMemories()));
        gd.recordSpellCast(player1.getId(), new IgniteMemories());
        gd.recordSpellCast(player2.getId(), new IgniteMemories());
        addIgniteMemoriesMana();

        harness.castSorcery(player1, 0, player2.getId());
        resolveSpellAndStorm();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        var target = harness.addToBattlefieldAndReturn(player2, new IgniteMemories());
        harness.setHand(player1, List.of(new IgniteMemories()));
        addIgniteMemoriesMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only target players");
    }

    private void addIgniteMemoriesMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void resolveSpellAndStorm() {
        for (int i = 0; i < 10 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }
    }
}
