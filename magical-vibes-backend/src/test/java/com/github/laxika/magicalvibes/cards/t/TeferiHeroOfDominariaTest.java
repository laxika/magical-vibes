package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetOpponentPermanentOnDrawEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetPermanentIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedUntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TeferiHeroEmblemEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TeferiHeroOfDominariaTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has three loyalty abilities")
    void hasThreeLoyaltyAbilities() {
        TeferiHeroOfDominaria card = new TeferiHeroOfDominaria();
        assertThat(card.getActivatedAbilities()).hasSize(3);
    }

    @Test
    @DisplayName("+1 ability has DrawCardEffect and RegisterDelayedUntapPermanentsEffect")
    void plusOneAbilityHasCorrectEffects() {
        TeferiHeroOfDominaria card = new TeferiHeroOfDominaria();
        var ability = card.getActivatedAbilities().get(0);

        assertThat(ability.getLoyaltyCost()).isEqualTo(1);
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(DrawCardEffect.class);
        assertThat(((DrawCardEffect) ability.getEffects().get(0)).amount()).isEqualTo(1);
        assertThat(ability.getEffects().get(1)).isInstanceOf(RegisterDelayedUntapPermanentsEffect.class);
        assertThat(((RegisterDelayedUntapPermanentsEffect) ability.getEffects().get(1)).count()).isEqualTo(2);
    }

    @Test
    @DisplayName("-3 ability has PutTargetPermanentIntoLibraryNFromTopEffect(2) with nonland filter")
    void minusThreeAbilityHasCorrectEffect() {
        TeferiHeroOfDominaria card = new TeferiHeroOfDominaria();
        var ability = card.getActivatedAbilities().get(1);

        assertThat(ability.getLoyaltyCost()).isEqualTo(-3);
        assertThat(ability.isNeedsTarget()).isTrue();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(PutTargetPermanentIntoLibraryNFromTopEffect.class);
        assertThat(((PutTargetPermanentIntoLibraryNFromTopEffect) ability.getEffects().getFirst()).position()).isEqualTo(2);
        assertThat(ability.getTargetFilter()).isInstanceOf(PermanentPredicateTargetFilter.class);
        PermanentPredicateTargetFilter filter = (PermanentPredicateTargetFilter) ability.getTargetFilter();
        assertThat(filter.predicate()).isInstanceOf(PermanentNotPredicate.class);
    }

    @Test
    @DisplayName("-8 ability has TeferiHeroEmblemEffect")
    void minusEightAbilityHasCorrectEffect() {
        TeferiHeroOfDominaria card = new TeferiHeroOfDominaria();
        var ability = card.getActivatedAbilities().get(2);

        assertThat(ability.getLoyaltyCost()).isEqualTo(-8);
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(TeferiHeroEmblemEffect.class);
    }

    // ===== +1 ability: Draw a card + delayed untap =====

    @Test
    @DisplayName("+1 draws a card and registers delayed untap trigger")
    void plusOneDrawsCardAndRegistersDelayedTrigger() {
        Permanent teferi = addReadyTeferi(player1);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        // Should have drawn a card
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        // Loyalty should increase
        assertThat(teferi.getLoyaltyCounters()).isEqualTo(5); // 4 + 1
        // Delayed trigger should be registered
        assertThat(gd.pendingDelayedUntapPermanents).hasSize(1);
        assertThat(gd.pendingDelayedUntapPermanents.getFirst().count()).isEqualTo(2);
        assertThat(gd.pendingDelayedUntapPermanents.getFirst().controllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("+1 delayed trigger untaps tapped lands at end step")
    void plusOneDelayedTriggerUntapsLandsAtEndStep() {
        Permanent teferi = addReadyTeferi(player1);

        // Add two tapped lands
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        Permanent plains = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Plains")).findFirst().orElseThrow();
        Permanent island = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Island")).findFirst().orElseThrow();
        plains.tap();
        island.tap();

        // Activate +1
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        // Delayed trigger registered
        assertThat(gd.pendingDelayedUntapPermanents).hasSize(1);

        // Advance to end step to fire delayed trigger
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        // Delayed trigger should be on the stack
        assertThat(gd.stack).isNotEmpty();

        // Resolve the trigger
        harness.passBothPriorities();

        // Both lands should be untapped
        assertThat(plains.isTapped()).isFalse();
        assertThat(island.isTapped()).isFalse();
        // Pending list should be cleared
        assertThat(gd.pendingDelayedUntapPermanents).isEmpty();
    }

    @Test
    @DisplayName("+1 delayed trigger untaps at most 2 lands when more than 2 are tapped")
    void plusOneDelayedTriggerUntapsAtMostTwoLands() {
        Permanent teferi = addReadyTeferi(player1);

        // Add three tapped lands
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        List<Permanent> lands = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Plains") || p.getCard().getName().equals("Island"))
                .toList();
        for (Permanent land : lands) {
            land.tap();
        }
        assertThat(lands).hasSize(3);

        // Activate +1
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        // Advance to end step
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.passBothPriorities();

        // Only 2 should be untapped, 1 remains tapped
        long tappedCount = lands.stream().filter(Permanent::isTapped).count();
        assertThat(tappedCount).isEqualTo(1);
    }

    // ===== -3 ability: Tuck nonland permanent =====

    @Test
    @DisplayName("-3 puts target nonland permanent third from top of owner's library")
    void minusThreePutsNonlandThirdFromTop() {
        Permanent teferi = addReadyTeferi(player1);

        // Add a creature on opponent's battlefield
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        // Ensure opponent has at least 3 cards in library so position matters
        int librarySize = gd.playerDecks.get(player2.getId()).size();

        harness.activateAbility(player1, 0, 1, null, bearsId);
        harness.passBothPriorities();

        // Bears should be gone from battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Bears should be third from top of opponent's library (index 2)
        List<Card> library = gd.playerDecks.get(player2.getId());
        assertThat(library).hasSizeGreaterThanOrEqualTo(3);
        assertThat(library.get(2).getName()).isEqualTo("Grizzly Bears");
        // Loyalty should decrease
        assertThat(teferi.getLoyaltyCounters()).isEqualTo(1); // 4 - 3
    }

    @Test
    @DisplayName("-3 can target own nonland permanent")
    void minusThreeCanTargetOwnPermanent() {
        Permanent teferi = addReadyTeferi(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, 1, null, bearsId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Third from top of player1's library
        assertThat(gd.playerDecks.get(player1.getId()).get(2).getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("-3 cannot target a land")
    void minusThreeCannotTargetLand() {
        addReadyTeferi(player1);
        harness.addToBattlefield(player2, new Plains());
        UUID plainsId = harness.getPermanentId(player2, "Plains");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, plainsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-3 handles small library by placing at bottom when library has fewer than 2 cards")
    void minusThreeHandlesSmallLibrary() {
        Permanent teferi = addReadyTeferi(player1);

        // Clear opponent's library to just 1 card
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(new GrizzlyBears());

        harness.addToBattlefield(player2, new LlanowarElves());
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");

        harness.activateAbility(player1, 0, 1, null, elvesId);
        harness.passBothPriorities();

        // With only 1 card in library, position 2 should clamp to library size
        List<Card> library = gd.playerDecks.get(player2.getId());
        assertThat(library).hasSize(2); // original card + tucked card
        // The tucked card should be at the end (position clamped)
        assertThat(library.get(1).getName()).isEqualTo("Llanowar Elves");
    }

    // ===== -8 ability: Emblem =====

    @Test
    @DisplayName("-8 creates emblem with ExileTargetOpponentPermanentOnDrawEffect")
    void minusEightCreatesEmblem() {
        Permanent teferi = addReadyTeferi(player1);
        teferi.setLoyaltyCounters(8);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.emblems).hasSize(1);
        Emblem emblem = gd.emblems.getFirst();
        assertThat(emblem.controllerId()).isEqualTo(player1.getId());
        assertThat(emblem.staticEffects()).hasSize(1);
        assertThat(emblem.staticEffects().getFirst()).isInstanceOf(ExileTargetOpponentPermanentOnDrawEffect.class);
        assertThat(emblem.sourceCard()).isNotNull();
    }

    @Test
    @DisplayName("-8 with 8 loyalty causes Teferi to go to graveyard and emblem persists")
    void emblemPersistsAfterTeferiDies() {
        Permanent teferi = addReadyTeferi(player1);
        teferi.setLoyaltyCounters(8);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        // Teferi should be gone (8 - 8 = 0 loyalty)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Teferi, Hero of Dominaria"));
        // Emblem persists
        assertThat(gd.emblems).hasSize(1);
    }

    @Test
    @DisplayName("Emblem triggers on controller draw, allowing exile of opponent's permanent")
    void emblemTriggersOnControllerDraw() {
        addReadyTeferi(player1);
        // Manually create the emblem
        Emblem emblem = new Emblem(player1.getId(), List.of(
                new ExileTargetOpponentPermanentOnDrawEffect()
        ), new TeferiHeroOfDominaria());
        gd.emblems.add(emblem);

        // Add target on opponent's battlefield
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        // Draw a card (triggers the emblem)
        harness.getDrawService().resolveDrawCard(gd, player1.getId());

        // Should be awaiting permanent choice for the emblem trigger target
        assertThat(gd.interaction.isAwaitingInput(AwaitingInput.PERMANENT_CHOICE)).isTrue();

        // Choose the opponent's bears
        harness.handlePermanentChosen(player1, bearsId);

        // Emblem trigger should be on stack
        assertThat(gd.stack).isNotEmpty();

        // Resolve the trigger
        harness.passBothPriorities();

        // Opponent's bears should be exiled
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerExiledCards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Emblem cannot target controller's own permanents")
    void emblemCannotTargetOwnPermanents() {
        addReadyTeferi(player1);
        Emblem emblem = new Emblem(player1.getId(), List.of(
                new ExileTargetOpponentPermanentOnDrawEffect()
        ), new TeferiHeroOfDominaria());
        gd.emblems.add(emblem);

        // Only add a permanent on controller's battlefield (no opponent permanents)
        harness.addToBattlefield(player1, new GrizzlyBears());

        // Draw a card
        harness.getDrawService().resolveDrawCard(gd, player1.getId());

        // No valid targets — emblem trigger should be skipped
        assertThat(gd.interaction.isAwaitingInput(AwaitingInput.PERMANENT_CHOICE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    // ===== Loyalty ability restrictions =====

    @Test
    @DisplayName("Cannot activate -8 with only 4 loyalty")
    void cannotActivateUltimateWithInsufficientLoyalty() {
        addReadyTeferi(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    @Test
    @DisplayName("Cannot activate two loyalty abilities on same planeswalker in one turn")
    void cannotActivateTwicePerTurn() {
        addReadyTeferi(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one loyalty ability");
    }

    // ===== Helpers =====

    private Permanent addReadyTeferi(com.github.laxika.magicalvibes.model.Player player) {
        TeferiHeroOfDominaria card = new TeferiHeroOfDominaria();
        Permanent perm = new Permanent(card);
        perm.setLoyaltyCounters(4);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
