package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
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

@CardUsed({TrueLovesKiss.class, FountainOfYouth.class, AngelicChorus.class, GrizzlyBears.class})
class TrueLovesKissTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles target artifact and draws a card")
    void exilesArtifactAndDrawsCard() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new TrueLovesKiss()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addMana();

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        assertThat(gameData.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Fountain of Youth"));
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Exiles target enchantment and draws a card")
    void exilesEnchantmentAndDrawsCard() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new TrueLovesKiss()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addMana();

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        assertThat(gameData.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Angelic Chorus"));
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TrueLovesKiss()));
        addMana();

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or enchantment");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
