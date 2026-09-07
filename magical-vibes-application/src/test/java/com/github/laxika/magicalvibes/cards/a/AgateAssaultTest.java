package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AgateAssault.class, GrizzlyBears.class, Millstone.class})
class AgateAssaultTest extends BaseCardTest {

    @Test
    @DisplayName("Damage mode deals 4 damage and exiles a creature that would die")
    void damageModeExilesCreatureThatWouldDie() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AgateAssault()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Damage mode cannot target an artifact")
    void damageModeRejectsArtifactTarget() {
        harness.addToBattlefield(player2, new Millstone());
        harness.setHand(player1, List.of(new AgateAssault()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Millstone");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Artifact mode exiles a target artifact")
    void artifactModeExilesArtifact() {
        harness.addToBattlefield(player2, new Millstone());
        harness.setHand(player1, List.of(new AgateAssault()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Millstone");
        harness.castSorcery(player1, 0, 1, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Millstone");
        harness.assertNotInGraveyard(player2, "Millstone");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Millstone"));
    }

    @Test
    @DisplayName("Artifact mode cannot target a nonartifact creature")
    void artifactModeRejectsCreatureTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AgateAssault()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
