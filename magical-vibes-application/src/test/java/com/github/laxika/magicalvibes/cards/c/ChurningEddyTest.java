package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChurningEddy.class, GrizzlyBears.class, Mountain.class})
class ChurningEddyTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the target creature and target land to their owners' hands")
    void returnsTargetCreatureAndLand() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new ChurningEddy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID landId = harness.getPermanentId(player2, "Mountain");
        harness.castSorcery(player1, 0, List.of(creatureId, landId));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Mountain");
    }

    @Test
    @DisplayName("Still returns the land if the creature target becomes illegal")
    void returnsLandIfCreatureTargetBecomesIllegal() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new ChurningEddy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID landId = harness.getPermanentId(player2, "Mountain");
        harness.castSorcery(player1, 0, List.of(creatureId, landId));
        gd.playerBattlefields.get(player2.getId()).removeIf(permanent -> permanent.getId().equals(creatureId));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInHand(player2, "Mountain");
    }

    @Test
    @DisplayName("Rejects a non-creature as the creature target")
    void rejectsNonCreatureCreatureTarget() {
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChurningEddy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID landId = harness.getPermanentId(player2, "Mountain");
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(landId, creatureId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
