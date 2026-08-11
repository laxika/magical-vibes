package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DistortingWakeTest extends BaseCardTest {

    @Test
    @DisplayName("X=2 returns two target nonland permanents to their owners' hands")
    void returnsXNonlandPermanents() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Spellbook());
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID artifactId = harness.getPermanentId(player2, "Spellbook");
        harness.setHand(player1, List.of(new DistortingWake()));
        harness.addMana(player1, ManaColor.BLUE, 5); // X=2: {2}{U}{U}{U}

        harness.castSorcery(player1, 0, 2, List.of(creatureId, artifactId));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Spellbook");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Spellbook");
    }

    @Test
    @DisplayName("X=0 returns no permanents")
    void xZeroReturnsNothing() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DistortingWake()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Island());
        UUID islandId = harness.getPermanentId(player2, "Island");
        harness.setHand(player1, List.of(new DistortingWake()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(islandId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent");
    }

    @Test
    @DisplayName("Cannot target more permanents than X")
    void cannotTargetMoreThanX() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Spellbook());
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID artifactId = harness.getPermanentId(player2, "Spellbook");
        harness.setHand(player1, List.of(new DistortingWake()));
        harness.addMana(player1, ManaColor.BLUE, 4); // X=1

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(creatureId, artifactId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between");
    }
}
