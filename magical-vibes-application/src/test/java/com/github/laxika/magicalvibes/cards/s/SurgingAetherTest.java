package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FrostRaptor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
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
        harness.castAndResolveInstant(player1, 0, targetId);

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

    @Test
    @DisplayName("A revealed matching spell may be declined and put on the bottom")
    void revealedMatchingSpellCanBeDeclined() {
        harness.addToBattlefield(player2, new SnowCoveredMountain());
        Card revealed = new SurgingAether();
        harness.setLibrary(player1, List.of(revealed));

        castSurgingAether(harness.getPermanentId(player2, "Snow-Covered Mountain"));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(revealed);
        harness.passBothPriorities();
        harness.assertInHand(player2, "Snow-Covered Mountain");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Ripple reveals only four cards and allows ordering the rest on the bottom")
    void revealsOnlyFourCardsAndOrdersThemOnBottom() {
        harness.addToBattlefield(player2, new SnowCoveredMountain());
        Card first = new FrostRaptor();
        Card second = new SnowCoveredMountain();
        Card third = new FrostRaptor();
        Card fourth = new SnowCoveredMountain();
        Card fifth = new SurgingAether();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth));

        castSurgingAether(harness.getPermanentId(player2, "Snow-Covered Mountain"));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 2, 1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fifth, fourth, third, second, first);
        harness.passBothPriorities();
        harness.assertInHand(player2, "Snow-Covered Mountain");
    }

    @Test
    @DisplayName("Accepting ripple with an empty library still allows the spell to resolve")
    void emptyLibraryDoesNotPreventBounce() {
        harness.addToBattlefield(player1, new SnowCoveredMountain());
        harness.setLibrary(player1, List.of());

        castSurgingAether(harness.getPermanentId(player1, "Snow-Covered Mountain"));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        harness.assertInHand(player1, "Snow-Covered Mountain");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Revealed spells without legal targets remain in the library, not the graveyard")
    void uncastableRevealedSpellsAreNotPutInGraveyard() {
        harness.addToBattlefield(player2, new SnowCoveredMountain());
        UUID targetId = harness.getPermanentId(player2, "Snow-Covered Mountain");
        Card first = new SurgingAether();
        Card second = new SurgingAether();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new SurgingAether(), new SurgingAether()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castInstant(player1, 0, targetId);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Snow-Covered Mountain");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second);
    }

    @Test
    @DisplayName("A ripple free cast cannot target a permanent with shroud")
    void rippleCannotTargetShroudedPermanent() {
        harness.addToBattlefield(player2, new FrostRaptor());
        UUID targetId = harness.getPermanentId(player2, "Frost Raptor");
        harness.setLibrary(player1, List.of(new SurgingAether()));
        harness.setHand(player1, List.of(new SurgingAether()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, targetId);
        gd.playerManaPools.get(player2.getId()).addSnowMana(ManaColor.BLUE, 2);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
    private void castSurgingAether(UUID targetId) {
        harness.setHand(player1, List.of(new SurgingAether()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
