package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AetherTide.class, RagingGoblin.class, Spellbook.class})
class AetherTideTest extends BaseCardTest {

    @Test
    @DisplayName("X=2 discards two creature cards and returns two target creatures")
    void returnsXCreaturesForTwoDiscardedCreatures() {
        harness.addToBattlefield(player2, new RagingGoblin());
        harness.addToBattlefield(player2, new RagingGoblin());
        harness.setHand(player1, List.of(new AetherTide(), new RagingGoblin(), new RagingGoblin()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        List<UUID> targetIds = gd.playerBattlefields.get(player2.getId()).stream()
                .map(permanent -> permanent.getId()).toList();

        harness.castSorceryWithDiscards(player1, 0, 2, targetIds, List.of(1, 2));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Aether Tide");
        harness.assertInHand(player2, "Raging Goblin");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Raging Goblin")).hasSize(2);
    }

    @Test
    @DisplayName("X=0 discards nothing and returns no creatures")
    void xZeroDoesNothing() {
        harness.addToBattlefield(player2, new RagingGoblin());
        harness.setHand(player1, List.of(new AetherTide(), new RagingGoblin()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorceryWithDiscards(player1, 0, 0, List.of(), List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Raging Goblin");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot discard a noncreature card to pay the additional cost")
    void cannotDiscardNoncreatureCard() {
        harness.addToBattlefield(player2, new RagingGoblin());
        harness.setHand(player1, List.of(new AetherTide(), new Spellbook()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Raging Goblin");

        assertThatThrownBy(() ->
                harness.castSorceryWithDiscards(player1, 0, 1, List.of(targetId), List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature cards");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player2, new Spellbook());
        harness.setHand(player1, List.of(new AetherTide(), new RagingGoblin()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Spellbook");

        assertThatThrownBy(() ->
                harness.castSorceryWithDiscards(player1, 0, 1, List.of(targetId), List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creatures");
    }

    @Test
    @DisplayName("Cannot target more creatures than X")
    void cannotTargetMoreThanX() {
        harness.addToBattlefield(player2, new RagingGoblin());
        harness.addToBattlefield(player2, new RagingGoblin());
        harness.setHand(player1, List.of(new AetherTide(), new RagingGoblin()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        List<UUID> targetIds = gd.playerBattlefields.get(player2.getId()).stream()
                .map(permanent -> permanent.getId()).toList();

        assertThatThrownBy(() ->
                harness.castSorceryWithDiscards(player1, 0, 1, targetIds, List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between");
    }

    @Test
    @DisplayName("Cannot target fewer creatures than X")
    void cannotTargetFewerThanX() {
        harness.addToBattlefield(player2, new RagingGoblin());
        harness.setHand(player1, List.of(new AetherTide(), new RagingGoblin(), new RagingGoblin()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Raging Goblin");

        assertThatThrownBy(() ->
                harness.castSorceryWithDiscards(player1, 0, 2, List.of(targetId), List.of(1, 2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between");
    }
}
