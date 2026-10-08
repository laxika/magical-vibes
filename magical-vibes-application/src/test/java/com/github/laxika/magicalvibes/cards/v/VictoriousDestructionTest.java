package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.d.DarksteelPlate;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VictoriousDestruction.class, RodOfRuin.class, Forest.class, GrizzlyBears.class, DarksteelPlate.class})
class VictoriousDestructionTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target artifact and its controller loses 1 life")
    void destroysArtifactAndControllerLosesLife() {
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.setHand(player1, List.of(new VictoriousDestruction()));
        harness.addMana(player1, ManaColor.RED, 5);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());
        UUID targetId = harness.getPermanentId(player2, "Rod of Ruin");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Rod of Ruin");
        harness.assertInGraveyard(player2, "Rod of Ruin");
        harness.assertLife(player2, lifeBefore - 1);
    }

    @Test
    @DisplayName("Destroys target land and its controller loses 1 life")
    void destroysLandAndControllerLosesLife() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new VictoriousDestruction()));
        harness.addMana(player1, ManaColor.RED, 5);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());
        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        harness.assertLife(player2, lifeBefore - 1);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VictoriousDestruction()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Controller loses life even when target is indestructible")
    void controllerLosesLifeEvenWhenIndestructible() {
        harness.addToBattlefield(player2, new DarksteelPlate());
        harness.setHand(player1, List.of(new VictoriousDestruction()));
        harness.addMana(player1, ManaColor.RED, 5);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());
        UUID targetId = harness.getPermanentId(player2, "Darksteel Plate");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertOnBattlefield(player2, "Darksteel Plate");
        harness.assertLife(player2, lifeBefore - 1);
    }

    @Test
    @DisplayName("Fizzles when target is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.setHand(player1, List.of(new VictoriousDestruction()));
        harness.addMana(player1, ManaColor.RED, 5);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());
        UUID targetId = harness.getPermanentId(player2, "Rod of Ruin");
        harness.castSorcery(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertLife(player2, lifeBefore);
    }

    @Test
    @DisplayName("Can destroy your own land and only you lose life")
    void destroysOwnLandAndCasterLosesLife() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new VictoriousDestruction()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player1, "Forest"));

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The destroyed land's controller loses life rather than its owner")
    void controllerLosesLifeRatherThanOwner() {
        Forest land = new Forest();
        land.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, land);
        harness.setHand(player1, List.of(new VictoriousDestruction()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Forest"));

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }
}
