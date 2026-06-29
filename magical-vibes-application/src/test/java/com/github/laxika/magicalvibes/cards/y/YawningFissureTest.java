package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class YawningFissureTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent with exactly one land loses it automatically")
    void opponentWithOneLandLosesIt() {
        harness.addToBattlefield(player2, new Forest());

        harness.setHand(player1, List.of(new YawningFissure()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Forest"));
    }

    @Test
    @DisplayName("Controller's lands are not sacrificed")
    void controllerLandsAreNotSacrificed() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());

        harness.setHand(player1, List.of(new YawningFissure()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Controller's lands should remain
        long p1Lands = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Mountain"))
                .count();
        assertThat(p1Lands).isEqualTo(2);

        // Opponent's land should be sacrificed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Forest"));
    }

    @Test
    @DisplayName("Opponent with multiple lands is prompted to choose which one to sacrifice")
    void opponentWithMultipleLandsChooses() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Mountain());

        harness.setHand(player1, List.of(new YawningFissure()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Opponent must choose 1 of 2 lands
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_PERMANENT_CHOICE);
        assertThat(gd.pendingForcedSacrificePlayerId).isEqualTo(player2.getId());
        assertThat(gd.pendingForcedSacrificeCount).isEqualTo(1);

        // Choose the Forest
        Permanent forest = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Forest"))
                .findFirst().orElseThrow();
        harness.handleMultiplePermanentsChosen(player2, List.of(forest.getId()));

        // Forest is gone, Mountain remains
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Forest"));
        harness.assertOnBattlefield(player2, "Mountain");
    }

    @Test
    @DisplayName("Opponent with no lands is unaffected")
    void opponentWithNoLandsIsUnaffected() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new YawningFissure()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Creature is unaffected — only lands are sacrificed
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Non-land permanents are not sacrificed")
    void nonLandPermanentsNotSacrificed() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());

        harness.setHand(player1, List.of(new YawningFissure()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Only the land is sacrificed, creature remains
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Forest"));
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
