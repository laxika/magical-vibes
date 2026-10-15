package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShatteringBlow.class, FountainOfYouth.class, GrizzlyBears.class})
class ShatteringBlowTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a target artifact")
    void exilesTargetArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new ShatteringBlow()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertNotInGraveyard(player2, "Fountain of Youth");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Fountain of Youth"));
    }

    @Test
    @DisplayName("Cannot target a nonartifact creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShatteringBlow()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can exile its controller's artifact using white mana")
    void exilesOwnArtifactUsingWhiteMana() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth()).getId();
        harness.setHand(player1, List.of(new ShatteringBlow()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
        harness.assertNotInGraveyard(player1, "Fountain of Youth");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Fountain of Youth"));
        harness.assertInGraveyard(player1, "Shattering Blow");
    }

    @Test
    @DisplayName("Does not resolve when its only target has already been exiled")
    void doesNotResolveAfterTargetLeavesBattlefield() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth()).getId();
        harness.setHand(player1, List.of(new ShatteringBlow(), new ShatteringBlow()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, targetId);
        harness.castInstant(player1, 0, targetId);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles (illegal target)")).isTrue();
        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertNotInGraveyard(player2, "Fountain of Youth");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(card -> card.getName().equals("Fountain of Youth"))
                .hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Shattering Blow"))
                .hasSize(2);
    }
}
