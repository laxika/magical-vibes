package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DeafeningSilence;
import com.github.laxika.magicalvibes.cards.g.GoldenEgg;
import com.github.laxika.magicalvibes.cards.w.WildwoodTracker;
import com.github.laxika.magicalvibes.cards.r.RovingKeep;
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

@CardUsed({TrueLovesKiss.class, GoldenEgg.class, DeafeningSilence.class, WildwoodTracker.class, RovingKeep.class})
class TrueLovesKissTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles target artifact and draws a card")
    void exilesArtifactAndDrawsCard() {
        harness.addToBattlefield(player2, new GoldenEgg());
        harness.setHand(player1, List.of(new TrueLovesKiss()));
        harness.setLibrary(player1, List.of(new WildwoodTracker()));
        addMana();

        UUID targetId = harness.getPermanentId(player2, "Golden Egg");
        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gameData = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Golden Egg");
        assertThat(gameData.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Golden Egg"));
        harness.assertInHand(player1, "Wildwood Tracker");
    }

    @Test
    @DisplayName("Exiles target enchantment and draws a card")
    void exilesEnchantmentAndDrawsCard() {
        harness.addToBattlefield(player2, new DeafeningSilence());
        harness.setHand(player1, List.of(new TrueLovesKiss()));
        harness.setLibrary(player1, List.of(new WildwoodTracker()));
        addMana();

        UUID targetId = harness.getPermanentId(player2, "Deafening Silence");
        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gameData = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Deafening Silence");
        assertThat(gameData.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Deafening Silence"));
        harness.assertInHand(player1, "Wildwood Tracker");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new WildwoodTracker());
        harness.setHand(player1, List.of(new TrueLovesKiss()));
        addMana();

        UUID targetId = harness.getPermanentId(player2, "Wildwood Tracker");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or enchantment");
    }

    @Test
    @DisplayName("Does not draw when the only target is sacrificed in response")
    void doesNotDrawWhenTargetLeavesBattlefield() {
        harness.addToBattlefield(player2, new GoldenEgg());
        harness.setHand(player1, List.of(new TrueLovesKiss()));
        harness.setLibrary(player1, List.of(new WildwoodTracker()));
        addMana();
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Golden Egg");
        harness.castInstant(player1, 0, targetId);
        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Wildwood Tracker");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "True Love's Kiss");
        harness.assertInGraveyard(player2, "Golden Egg");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can exile an artifact creature and draws exactly one card")
    void exilesArtifactCreature() {
        harness.addToBattlefield(player2, new RovingKeep());
        harness.setHand(player1, List.of(new TrueLovesKiss()));
        harness.setLibrary(player1, List.of(new WildwoodTracker(), new WildwoodTracker()));
        addMana();

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Roving Keep"));

        harness.assertNotOnBattlefield(player2, "Roving Keep");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Roving Keep"));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Can exile its controller's own artifact")
    void exilesOwnArtifact() {
        harness.addToBattlefield(player1, new GoldenEgg());
        harness.setHand(player1, List.of(new TrueLovesKiss()));
        harness.setLibrary(player1, List.of(new WildwoodTracker()));
        addMana();

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Golden Egg"));

        harness.assertNotOnBattlefield(player1, "Golden Egg");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Golden Egg"));
        harness.assertInHand(player1, "Wildwood Tracker");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
