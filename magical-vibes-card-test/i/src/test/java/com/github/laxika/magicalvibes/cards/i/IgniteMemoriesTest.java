package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CoalStoker;
import com.github.laxika.magicalvibes.cards.c.Conflagrate;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IgniteMemories.class, CoalStoker.class, Conflagrate.class, Mountain.class})
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
    @DisplayName("Deals no damage when the target player's hand is empty")
    void dealsNoDamageWhenTargetPlayersHandIsEmpty() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new IgniteMemories()));
        harness.setHand(player2, List.of());
        addIgniteMemoriesMana();

        harness.castSorcery(player1, 0, player2.getId());
        resolveSpellAndStorm();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
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
    @DisplayName("Storm copies may choose a new target player")
    void stormCopyMayChooseNewTargetPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new IgniteMemories(), new IgniteMemories()));
        harness.setHand(player2, List.of(new IgniteMemories()));
        gd.recordSpellCast(player1.getId(), new IgniteMemories());
        addIgniteMemoriesMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        var target = harness.addToBattlefieldAndReturn(player2, new CoalStoker());
        harness.setHand(player1, List.of(new IgniteMemories()));
        addIgniteMemoriesMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only target players");
    }

    @Test
    @DisplayName("Revealing a land deals no damage and leaves it in hand")
    void revealingLandDealsNoDamage() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new IgniteMemories()));
        harness.setHand(player2, List.of(new Mountain()));
        addIgniteMemoriesMana();

        harness.castSorcery(player1, 0, player2.getId());
        resolveSpellAndStorm();

        harness.assertLife(player2, 20);
        harness.assertInHand(player2, "Mountain");
        assertThat(gameLogContains("reveals Mountain at random")).isTrue();
    }

    @Test
    @DisplayName("X symbols in a revealed card's mana cost contribute zero")
    void revealedXSpellUsesManaValueInHand() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new IgniteMemories()));
        harness.setHand(player2, List.of(new Conflagrate()));
        addIgniteMemoriesMana();

        harness.castSorcery(player1, 0, player2.getId());
        resolveSpellAndStorm();

        harness.assertLife(player2, 19);
        harness.assertInHand(player2, "Conflagrate");
    }

    @Test
    @DisplayName("The original spell may target its caster")
    void originalSpellCanTargetCaster() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new IgniteMemories(), new CoalStoker()));
        addIgniteMemoriesMana();

        harness.castSorcery(player1, 0, player1.getId());
        resolveSpellAndStorm();

        harness.assertLife(player1, 16);
        harness.assertInHand(player1, "Coal Stoker");
    }

    private void addIgniteMemoriesMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void resolveSpellAndStorm() {
        while (!gd.stack.isEmpty()) {
            if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
                harness.handleMayAbilityChosen(player1, false);
            } else {
                resolveAllTriggers();
            }
        }
    }
}
