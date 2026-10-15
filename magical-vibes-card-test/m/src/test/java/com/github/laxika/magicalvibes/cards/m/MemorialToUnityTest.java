package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.b.BalothGorger;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.ShivanFire;
import com.github.laxika.magicalvibes.cards.s.Swamp;
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

@CardUsed({MemorialToUnity.class, LlanowarElves.class, BalothGorger.class, ShivanFire.class, Plains.class, Swamp.class})
class MemorialToUnityTest extends BaseCardTest {
    @Test
    @DisplayName("Activating ability sacrifices Memorial to Unity and offers creature cards from top five")
    void activatingOffersCreatureCards() {
        harness.addToBattlefield(player1, new MemorialToUnity());
        harness.setLibrary(player1, List.of(
                new LlanowarElves(),
                new ShivanFire(),
                new BalothGorger(),
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
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.LibrarySearch search) {
            assertThat(search.params().playerId()).isEqualTo(player1.getId());
            assertThat(search.params().canFailToFind()).isTrue();
            assertThat(search.params().cards().stream().map(Card::getName))
                    .containsExactlyInAnyOrder("Llanowar Elves", "Baloth Gorger");
        } else {
            var choice = gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
            assertThat(choice.playerId()).isEqualTo(player1.getId());
            assertThat(choice.minCount()).isZero();
            assertThat(choice.maxCount()).isEqualTo(1);
            assertThat(choice.allCards().stream().filter(card -> choice.validCardIds().contains(card.getId()))
                    .map(Card::getName)).containsExactlyInAnyOrder("Llanowar Elves", "Baloth Gorger");
        }
    }

    @Test
    @DisplayName("Choosing a creature puts it into hand and randomly bottoms the rest")
    void choosingCreaturePutsIntoHand() {
        harness.addToBattlefield(player1, new MemorialToUnity());
        harness.setLibrary(player1, List.of(
                new LlanowarElves(),
                new ShivanFire(),
                new BalothGorger(),
                new Plains(),
                new Swamp()
        ));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Choose Llanowar Elves
        chooseCreature(0);

        harness.assertInHand(player1, "Llanowar Elves");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).stream().map(Card::getName))
                .containsExactlyInAnyOrder("Shivan Fire", "Baloth Gorger", "Plains", "Swamp");
    }

    @Test
    @DisplayName("You may choose no creature card")
    void mayChooseNoCreature() {
        harness.addToBattlefield(player1, new MemorialToUnity());
        harness.setLibrary(player1, List.of(
                new LlanowarElves(),
                new ShivanFire(),
                new BalothGorger(),
                new Plains(),
                new Swamp()
        ));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        chooseCreature(-1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("If no creature cards in top five, randomly bottom all five")
    void noCreaturesGoToBottomWithoutReorder() {
        harness.addToBattlefield(player1, new MemorialToUnity());
        harness.setLibrary(player1, List.of(
                new ShivanFire(),
                new Plains(),
                new Swamp(),
                new ShivanFire(),
                new Plains()
        ));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Memorial to Unity is sacrificed as a cost before resolution")
    void sacrificedAsCostBeforeResolution() {
        harness.addToBattlefield(player1, new MemorialToUnity());
        harness.setLibrary(player1, List.of(new LlanowarElves(), new ShivanFire(), new BalothGorger(), new Plains(), new Swamp()));
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
        // Only one green mana is available; the cost is {2}{G}.

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


    @Test
    void entersTapped() {
        harness.setHand(player1, List.of(new MemorialToUnity()));
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    void tapProducesGreenManaWithoutUsingStack() {
        harness.addToBattlefield(player1, new MemorialToUnity());
        harness.tapPermanent(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.assertNotInGraveyard(player1, "Memorial to Unity");
    }

    @Test
    void shortLibraryAllowsDecliningTheOnlyCreature() {
        harness.addToBattlefield(player1, new MemorialToUnity());
        var creature = new LlanowarElves();
        var land = new Plains();
        harness.setLibrary(player1, List.of(creature, land));
        int handSize = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        chooseCreature(-1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(creature, land);
    }

    @Test
    void emptyLibraryResolvesWithoutDrawingOrLosing() {
        harness.addToBattlefield(player1, new MemorialToUnity());
        harness.setLibrary(player1, List.of());
        int handSize = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Memorial to Unity");
        assertThat(gd.status).isEqualTo(com.github.laxika.magicalvibes.model.GameStatus.RUNNING);
    }

    @Test
    void onlyTheTopFiveAreExaminedAndTheUnexaminedCardStaysOnTop() {
        harness.addToBattlefield(player1, new MemorialToUnity());
        var sixth = new BalothGorger();
        harness.setLibrary(player1, List.of(new ShivanFire(), new Plains(), new Swamp(),
                new Plains(), new Swamp(), sixth));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6).first().isSameAs(sixth);
        harness.assertNotInHand(player1, "Baloth Gorger");
    }

    private void chooseCreature(int index) {
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.LibraryRevealChoice choice) {
            harness.handleMultipleCardsChosen(player1,
                    index < 0 ? List.of() : List.of(choice.validCardIds().get(index)));
        } else {
            harness.handleCardChosen(player1, index);
        }
    }
}
