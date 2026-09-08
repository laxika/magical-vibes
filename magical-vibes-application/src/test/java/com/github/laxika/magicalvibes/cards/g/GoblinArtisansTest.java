package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoblinArtisansTest extends BaseCardTest {

    @Test
    @DisplayName("Flips a coin and draws or counters the targeted artifact spell")
    void flipsForDrawOrCounter() {
        addCreatureReady(player1, new GoblinArtisans());
        Ornithopter ornithopter = new Ornithopter();
        harness.setHand(player1, List.of(ornithopter));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.activateAbility(player1, 0, null, ornithopter.getId());
        harness.passBothPriorities();

        List<String> logs = gd.gameLog.stream().map(GameLogEntry::plainText).toList();
        assertThat(logs).anyMatch(log -> log.contains("coin flip for Goblin Artisans"));
        if (logs.stream().anyMatch(log -> log.contains("wins the coin flip for Goblin Artisans"))) {
            assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
            assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(ornithopter.getId()));
        } else {
            harness.assertInGraveyard(player1, "Ornithopter");
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        }
    }

    @Test
    @DisplayName("Cannot target an artifact spell already targeted by another Goblin Artisans")
    void cannotTargetArtifactSpellAlreadyTargetedByAnotherArtisan() {
        addCreatureReady(player1, new GoblinArtisans());
        addCreatureReady(player1, new GoblinArtisans());
        Ornithopter ornithopter = new Ornithopter();
        harness.setHand(player1, List.of(ornithopter));

        harness.castCreature(player1, 0);
        harness.activateAbility(player1, 0, null, ornithopter.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, ornithopter.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact spell");
    }
}
