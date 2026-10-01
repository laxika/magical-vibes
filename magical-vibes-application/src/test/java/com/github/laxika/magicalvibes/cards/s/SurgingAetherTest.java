package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FrostRaptor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SurgingAether.class, FrostRaptor.class, SnowCoveredMountain.class})
class SurgingAetherTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the target permanent to its owner's hand")
    void returnsTargetPermanentToHand() {
        harness.addToBattlefield(player2, new SnowCoveredMountain());
        UUID targetId = harness.getPermanentId(player2, "Snow-Covered Mountain");

        castSurgingAether(targetId);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Snow-Covered Mountain");
        harness.assertInHand(player2, "Snow-Covered Mountain");
    }

    @Test
    @DisplayName("Ripple casts a revealed Surging Aether at a creature without paying its mana cost")
    void rippleCastsMatchingAetherWithoutPayingMana() {
        harness.addToBattlefield(player2, new FrostRaptor());
        UUID targetId = harness.getPermanentId(player2, "Frost Raptor");
        harness.setLibrary(player1, List.of(new SurgingAether(), new SnowCoveredMountain()));

        castSurgingAether(targetId);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, targetId);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Snow-Covered Mountain");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Surging Aether", "Surging Aether");
        harness.assertInHand(player2, "Frost Raptor");
    }

    @Test
    @DisplayName("Ripple can free-cast Surging Aether targeting a noncreature permanent")
    void rippleFreeCastCanTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new SnowCoveredMountain());
        UUID targetId = harness.getPermanentId(player2, "Snow-Covered Mountain");
        harness.setLibrary(player1, List.of(new SurgingAether(), new SnowCoveredMountain()));

        castSurgingAether(targetId);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction())
                .as("a revealed Surging Aether should accept any permanent target")
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, targetId);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Snow-Covered Mountain");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Surging Aether", "Surging Aether");
        harness.assertInHand(player2, "Snow-Covered Mountain");
    }

    @Test
    @DisplayName("Ripple can be declined without revealing the library")
    void rippleCanBeDeclined() {
        harness.addToBattlefield(player2, new SnowCoveredMountain());
        UUID targetId = harness.getPermanentId(player2, "Snow-Covered Mountain");
        List<Card> library = List.of(new SnowCoveredMountain(), new SnowCoveredMountain());
        harness.setLibrary(player1, library);

        harness.setHand(player1, List.of(new SurgingAether()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new SurgingAether()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castSurgingAether(UUID targetId) {
        harness.setHand(player1, List.of(new SurgingAether()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
