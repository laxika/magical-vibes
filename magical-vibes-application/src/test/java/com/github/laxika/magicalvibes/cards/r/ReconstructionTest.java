package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Reconstruction.class, AngelsFeather.class, HolyDay.class})
class ReconstructionTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target artifact card from your graveyard to your hand")
    void returnsTargetArtifactFromGraveyardToHand() {
        Card artifact = new AngelsFeather();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new Reconstruction()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, artifact.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(artifact.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(artifact.getId()));
        harness.assertInGraveyard(player1, "Reconstruction");
    }

    @Test
    @DisplayName("Cannot target a nonartifact card in your graveyard")
    void cannotTargetNonArtifactCard() {
        Card nonartifact = new HolyDay();
        harness.setGraveyard(player1, List.of(nonartifact));
        harness.setHand(player1, List.of(new Reconstruction()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, nonartifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an artifact card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card artifact = new AngelsFeather();
        harness.setGraveyard(player2, List.of(artifact));
        harness.setHand(player1, List.of(new Reconstruction()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");
    }

    @Test
    @DisplayName("Fizzles if the targeted artifact card leaves the graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyardBeforeResolution() {
        Card artifact = new AngelsFeather();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new Reconstruction()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, artifact.getId());
        harness.getGameData().playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(c -> c.getId().equals(artifact.getId()));
        harness.assertInGraveyard(player1, "Reconstruction");
    }
}
