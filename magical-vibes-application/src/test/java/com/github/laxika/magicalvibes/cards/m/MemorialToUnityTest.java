package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemorialToUnityTest extends BaseCardTest {

    @Test
    @DisplayName("Memorial to Unity has correct activated ability")
    void hasCorrectActivatedAbility() {
        MemorialToUnity card = new MemorialToUnity();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isEqualTo("{2}{G}");
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(SacrificeSelfCost.class);
        assertThat(ability.getEffects().get(1)).isInstanceOf(LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect.class);
        LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect effect =
                (LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect) ability.getEffects().get(1);
        assertThat(effect.count()).isEqualTo(5);
        assertThat(effect.predicate()).isEqualTo(new CardTypePredicate(CardType.CREATURE));
    }

    @Test
    @DisplayName("Activating ability sacrifices Memorial to Unity and offers creature cards from top five")
    void activatingOffersCreatureCards() {
        harness.addToBattlefield(player1, new MemorialToUnity());
        setupTopFive(List.of(
                new LlanowarElves(),
                new Shock(),
                new GrizzlyBears(),
                new Plains(),
                new Swamp()
        ));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Memorial should be sacrificed
        harness.assertNotOnBattlefield(player1, "Memorial to Unity");
        harness.assertInGraveyard(player1, "Memorial to Unity");

        // Should offer creature cards from top five
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.librarySearch().canFailToFind()).isTrue();
        assertThat(gd.interaction.librarySearch().cards()).hasSize(2);
        assertThat(gd.interaction.librarySearch().cards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Llanowar Elves", "Grizzly Bears");
    }

    @Test
    @DisplayName("Choosing a creature puts it into hand then orders rest on bottom")
    void choosingCreaturePutsIntoHand() {
        harness.addToBattlefield(player1, new MemorialToUnity());
        setupTopFive(List.of(
                new LlanowarElves(),
                new Shock(),
                new GrizzlyBears(),
                new Plains(),
                new Swamp()
        ));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Choose Llanowar Elves
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Llanowar Elves"));
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        assertThat(gd.interaction.libraryView().reorderCards()).hasSize(4);
    }

    @Test
    @DisplayName("You may choose no creature card")
    void mayChooseNoCreature() {
        harness.addToBattlefield(player1, new MemorialToUnity());
        setupTopFive(List.of(
                new LlanowarElves(),
                new Shock(),
                new GrizzlyBears(),
                new Plains(),
                new Swamp()
        ));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.getGameService().handleLibraryCardChosen(gd, player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        assertThat(gd.interaction.libraryView().reorderCards()).hasSize(5);
    }

    @Test
    @DisplayName("If no creature cards in top five, directly go to reorder")
    void noCreaturesGoesToReorder() {
        harness.addToBattlefield(player1, new MemorialToUnity());
        setupTopFive(List.of(
                new Shock(),
                new Plains(),
                new Swamp(),
                new Shock(),
                new Plains()
        ));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        assertThat(gd.interaction.libraryView().reorderCards()).hasSize(5);
    }

    @Test
    @DisplayName("Memorial to Unity is sacrificed as a cost before resolution")
    void sacrificedAsCostBeforeResolution() {
        harness.addToBattlefield(player1, new MemorialToUnity());
        setupTopFive(List.of(new LlanowarElves(), new Shock(), new GrizzlyBears(), new Plains(), new Swamp()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        // Before resolution, Memorial should already be sacrificed
        harness.assertNotOnBattlefield(player1, "Memorial to Unity");
        harness.assertInGraveyard(player1, "Memorial to Unity");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new MemorialToUnity());
        harness.addMana(player1, ManaColor.GREEN, 1);
        // Only 1G, need 2G+1 colorless

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        harness.addToBattlefield(player1, new MemorialToUnity());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gd.playerBattlefields.get(player1.getId()).getFirst().tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    private void setupTopFive(List<Card> cards) {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }
}
