package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfAndGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ElixirOfImmortalityTest extends BaseCardTest {

    // ===== Ability structure =====

    @Test
    @DisplayName("Elixir of Immortality has correct activated ability")
    void hasCorrectAbility() {
        ElixirOfImmortality card = new ElixirOfImmortality();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isEqualTo("{2}");
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects())
                .hasSize(2)
                .anyMatch(e -> e instanceof GainLifeEffect ge && ge.amount() == 5)
                .anyMatch(e -> e instanceof ShuffleSelfAndGraveyardIntoLibraryEffect);
    }

    // ===== Activation =====

    @Test
    @DisplayName("Activating ability puts it on the stack and taps the artifact")
    void activatingPutsOnStack() {
        Permanent elixir = addReadyElixir(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(elixir.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyElixir(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate when tapped")
    void cannotActivateWhenTapped() {
        Permanent elixir = addReadyElixir(player1);
        elixir.tap();
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Resolution — life gain =====

    @Test
    @DisplayName("Resolving gains 5 life")
    void resolvingGainsFiveLife() {
        addReadyElixir(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLife(player1, 15);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    // ===== Resolution — shuffle self and graveyard =====

    @Test
    @DisplayName("Resolving shuffles Elixir and graveyard into library")
    void resolvingShufflesSelfAndGraveyardIntoLibrary() {
        addReadyElixir(player1);
        Card bear1 = new GrizzlyBears();
        Card bear2 = new GiantSpider();
        harness.setGraveyard(player1, List.of(bear1, bear2));
        harness.addMana(player1, ManaColor.WHITE, 2);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Elixir should not be on battlefield
        harness.assertNotOnBattlefield(player1, "Elixir of Immortality");
        // Graveyard should be empty
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        // Deck should have grown by 3 (Elixir + 2 graveyard cards)
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore + 3);
        // Elixir should be in library
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Elixir of Immortality"));
    }

    @Test
    @DisplayName("Resolving with empty graveyard still shuffles Elixir into library")
    void emptyGraveyardStillShufflesSelf() {
        addReadyElixir(player1);
        harness.setGraveyard(player1, new ArrayList<>());
        harness.addMana(player1, ManaColor.WHITE, 2);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Elixir should not be on battlefield
        harness.assertNotOnBattlefield(player1, "Elixir of Immortality");
        // Deck should have grown by 1 (just the Elixir)
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore + 1);
        // Elixir should be in library
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Elixir of Immortality"));
    }

    @Test
    @DisplayName("If Elixir leaves battlefield before resolution, graveyard is still shuffled")
    void elixirRemovedBeforeResolutionStillShufflesGraveyard() {
        addReadyElixir(player1);
        Card bear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bear));
        harness.addMana(player1, ManaColor.WHITE, 2);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        // Remove Elixir from battlefield before resolution (e.g. destroyed in response)
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        // Graveyard cards (bear + elixir that was destroyed) should be in library now
        // The graveyard should be empty after shuffling
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        // Library should have the graveyard cards
        assertThat(gd.playerDecks.get(player1.getId()).size()).isGreaterThanOrEqualTo(deckSizeBefore + 1);
    }

    // ===== Stack is empty after resolution =====

    @Test
    @DisplayName("Stack is empty after resolution")
    void stackIsEmptyAfterResolution() {
        addReadyElixir(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    // ===== Log message =====

    @Test
    @DisplayName("Log contains shuffle message")
    void logContainsShuffleMessage() {
        addReadyElixir(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("shuffles") && log.contains("Elixir of Immortality"));
    }

    // ===== Helpers =====

    private Permanent addReadyElixir(Player player) {
        ElixirOfImmortality card = new ElixirOfImmortality();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
