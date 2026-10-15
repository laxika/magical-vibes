package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Chronomaton;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Smelt.class, FountainOfYouth.class, GrizzlyBears.class, Chronomaton.class, StuffyDoll.class})
class SmeltTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Smelt destroys target artifact")
    void destroysArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new Smelt()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Fountain of Youth");
        harness.assertInGraveyard(player1, "Smelt");
    }

    @Test
    @DisplayName("Smelt can destroy an artifact its controller owns")
    void canDestroyOwnArtifact() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Smelt()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertInGraveyard(player1, "Fountain of Youth");
    }

    @Test
    @DisplayName("Smelt fizzles when its target leaves before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new Smelt()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castInstant(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Smelt");
    }

    @Test
    @DisplayName("Smelt cannot target a nonartifact creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Smelt()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Smelt destroys an artifact creature")
    void destroysArtifactCreature() {
        harness.addToBattlefield(player2, new Chronomaton());
        harness.setHand(player1, List.of(new Smelt()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Chronomaton");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Chronomaton");
        harness.assertInGraveyard(player2, "Chronomaton");
        harness.assertInGraveyard(player1, "Smelt");
    }

    @Test
    @DisplayName("Smelt can target an indestructible artifact but cannot destroy it")
    void cannotDestroyIndestructibleArtifact() {
        harness.addToBattlefield(player2, new StuffyDoll());
        harness.setHand(player1, List.of(new Smelt()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Stuffy Doll");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertOnBattlefield(player2, "Stuffy Doll");
        harness.assertNotInGraveyard(player2, "Stuffy Doll");
        harness.assertInGraveyard(player1, "Smelt");
        assertThat(harness.getGameData().gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains("fizzles"));
    }
}
