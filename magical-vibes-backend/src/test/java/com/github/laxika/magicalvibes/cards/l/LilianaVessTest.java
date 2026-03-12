package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardToTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LilianaVessTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has three loyalty abilities")
    void hasThreeLoyaltyAbilities() {
        LilianaVess card = new LilianaVess();
        assertThat(card.getActivatedAbilities()).hasSize(3);
    }

    @Test
    @DisplayName("+1 ability has TargetPlayerDiscardsEffect(1)")
    void plusOneAbilityHasCorrectEffect() {
        LilianaVess card = new LilianaVess();
        var ability = card.getActivatedAbilities().get(0);

        assertThat(ability.getLoyaltyCost()).isEqualTo(1);
        assertThat(ability.isNeedsTarget()).isTrue();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(TargetPlayerDiscardsEffect.class);
        assertThat(((TargetPlayerDiscardsEffect) ability.getEffects().getFirst()).amount()).isEqualTo(1);
    }

    @Test
    @DisplayName("-2 ability has SearchLibraryForCardToTopOfLibraryEffect")
    void minusTwoAbilityHasCorrectEffect() {
        LilianaVess card = new LilianaVess();
        var ability = card.getActivatedAbilities().get(1);

        assertThat(ability.getLoyaltyCost()).isEqualTo(-2);
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(SearchLibraryForCardToTopOfLibraryEffect.class);
    }

    @Test
    @DisplayName("-8 ability has ReturnCardFromGraveyardEffect for all creatures from all graveyards")
    void minusEightAbilityHasCorrectEffect() {
        LilianaVess card = new LilianaVess();
        var ability = card.getActivatedAbilities().get(2);

        assertThat(ability.getLoyaltyCost()).isEqualTo(-8);
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(ReturnCardFromGraveyardEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts planeswalker spell on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new LilianaVess()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castPlaneswalker(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.PLANESWALKER_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Liliana Vess");
    }

    @Test
    @DisplayName("Resolving puts planeswalker on battlefield with initial loyalty 5")
    void resolvingEntersBattlefieldWithLoyalty() {
        harness.setHand(player1, List.of(new LilianaVess()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        assertThat(bf).anyMatch(p -> p.getCard().getName().equals("Liliana Vess"));
        Permanent liliana = bf.stream().filter(p -> p.getCard().getName().equals("Liliana Vess")).findFirst().orElseThrow();
        assertThat(liliana.getLoyaltyCounters()).isEqualTo(5);
        assertThat(liliana.isSummoningSick()).isFalse();
    }

    // ===== +1 ability: Target player discards a card =====

    @Test
    @DisplayName("+1 ability makes target player discard a card and increases loyalty")
    void plusOneMakesTargetPlayerDiscard() {
        Permanent liliana = addReadyLiliana(player1);
        harness.setHand(player2, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(liliana.getLoyaltyCounters()).isEqualTo(6); // 5 + 1
        // Player is prompted to choose a card to discard
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.interaction.cardChoice().playerId()).isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("+1 ability can target self")
    void plusOneCanTargetSelf() {
        Permanent liliana = addReadyLiliana(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, player1.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(liliana.getLoyaltyCounters()).isEqualTo(6); // 5 + 1
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    // ===== -2 ability: Search library for a card to top =====

    @Test
    @DisplayName("-2 ability triggers library search and decreases loyalty")
    void minusTwoTriggersLibrarySearch() {
        Permanent liliana = addReadyLiliana(player1);
        setupLibrary();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(liliana.getLoyaltyCounters()).isEqualTo(3); // 5 - 2
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        // All cards from library should be offered (unrestricted search)
        assertThat(gd.interaction.librarySearch().cards()).hasSize(4);
    }

    @Test
    @DisplayName("-2 ability puts chosen card on top of library")
    void minusTwoPutsCardOnTop() {
        Permanent liliana = addReadyLiliana(player1);
        setupLibrary();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Card> offered = gd.interaction.librarySearch().cards();
        // Find Grizzly Bears in the offered cards
        int bearsIndex = -1;
        for (int i = 0; i < offered.size(); i++) {
            if (offered.get(i).getName().equals("Grizzly Bears")) {
                bearsIndex = i;
                break;
            }
        }
        assertThat(bearsIndex).isGreaterThanOrEqualTo(0);

        gs.handleLibraryCardChosen(gd, player1, bearsIndex);

        // The chosen card should be on top of the library
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).isNotEmpty();
        assertThat(deck.getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("-2 ability is unrestricted search (cannot fail to find)")
    void minusTwoCannotFailToFind() {
        Permanent liliana = addReadyLiliana(player1);
        setupLibrary();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.librarySearch().canFailToFind()).isFalse();
    }

    // ===== -8 ability: Put all creature cards from all graveyards onto battlefield =====

    @Test
    @DisplayName("-8 ability puts all creature cards from all graveyards onto battlefield under controller's control")
    void minusEightPutsAllCreaturesFromAllGraveyards() {
        Permanent liliana = addReadyLiliana(player1);
        liliana.setLoyaltyCounters(8);

        // Put creature cards into both graveyards
        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        GrizzlyBears bears3 = new GrizzlyBears();
        gd.playerGraveyards.get(player1.getId()).add(bears1);
        gd.playerGraveyards.get(player2.getId()).add(bears2);
        gd.playerGraveyards.get(player2.getId()).add(bears3);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Liliana should have 0 loyalty (8 - 8) and be in graveyard
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Liliana Vess"));

        // All three creatures should be on player1's battlefield
        long bearsOnBf = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .count();
        assertThat(bearsOnBf).isEqualTo(3);

        // Both graveyards should have no creature cards remaining
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("-8 ability does not put non-creature cards onto battlefield")
    void minusEightDoesNotPutNonCreatures() {
        Permanent liliana = addReadyLiliana(player1);
        liliana.setLoyaltyCounters(8);

        // Put a non-creature card into graveyard
        gd.playerGraveyards.get(player2.getId()).add(new Plains());
        gd.playerGraveyards.get(player2.getId()).add(new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Only the creature should be on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Plains"));

        // Plains should still be in opponent's graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Plains"));
    }

    @Test
    @DisplayName("Cannot use -8 when loyalty is only 5")
    void cannotActivateMinusEightWithInsufficientLoyalty() {
        Permanent liliana = addReadyLiliana(player1);
        assertThat(liliana.getLoyaltyCounters()).isEqualTo(5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    // ===== Loyalty ability restrictions =====

    @Test
    @DisplayName("Cannot activate loyalty ability during opponent's turn")
    void cannotActivateOnOpponentsTurn() {
        addReadyLiliana(player1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your turn");
    }

    @Test
    @DisplayName("Cannot activate two loyalty abilities on same planeswalker in one turn")
    void cannotActivateTwicePerTurn() {
        addReadyLiliana(player1);
        harness.setHand(player2, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one loyalty ability");
    }

    // ===== Helpers =====

    private Permanent addReadyLiliana(Player player) {
        LilianaVess card = new LilianaVess();
        Permanent perm = new Permanent(card);
        perm.setLoyaltyCounters(5);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private void setupLibrary() {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Swamp(), new GrizzlyBears(), new GrizzlyBears()));
    }
}
