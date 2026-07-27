package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DesertTwisterTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving destroys target creature")
    void destroysCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DesertTwister()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can destroy a land — target is any permanent")
    void destroysLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new DesertTwister()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Can destroy an artifact")
    void destroysArtifact() {
        harness.addToBattlefield(player2, new Spellbook());
        harness.setHand(player1, List.of(new DesertTwister()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        UUID targetId = harness.getPermanentId(player2, "Spellbook");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Spellbook");
    }

    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DesertTwister()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);

        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(harness.getGameData().gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("fizzles"));
    }
}
