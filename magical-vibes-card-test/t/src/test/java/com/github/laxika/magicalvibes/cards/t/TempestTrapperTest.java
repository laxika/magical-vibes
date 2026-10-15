package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TempestTrapper.class, LightningBolt.class, Divination.class, Island.class})
class TempestTrapperTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability adds two mana in separately chosen colors for instants and sorceries")
    void tapAbilityAddsRestrictedManaInAnyColorCombination() {
        addCreatureReady(player1, new TempestTrapper());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColored(ManaColor.BLUE))
                .isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColored(ManaColor.RED))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("The third spell exiles a random library card with free-play permission")
    void thirdSpellExilesLibraryCardForFree() {
        addCreatureReady(player1, new TempestTrapper());
        Card exiledCard = new LightningBolt();
        harness.setLibrary(player1, List.of(exiledCard));
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(exiledCard);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(exiledCard);
        assertThat(gd.exilePlayPermissions).containsEntry(exiledCard.getId(), player1.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).contains(exiledCard.getId());
    }

    @Test
    @DisplayName("Free-play permission lets the third-spell card be cast without mana")
    void thirdSpellCardCanBeCastWithoutMana() {
        addCreatureReady(player1, new TempestTrapper());
        Card exiledCard = new LightningBolt();
        harness.setLibrary(player1, List.of(exiledCard));
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        gd.playerManaPools.get(player1.getId()).clear();
        harness.castFromExile(player1, exiledCard.getId(), player2.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(exiledCard);
    }

    @Test
    @DisplayName("Restricted mana pays for instant spells")
    void restrictedManaCanCastInstant() {
        addCreatureReady(player1, new TempestTrapper());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "RED");

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Lightning Bolt");
    }

    @Test
    @DisplayName("Restricted mana pays colored and generic sorcery costs")
    void restrictedManaCanCastSorcery() {
        addCreatureReady(player1, new TempestTrapper());
        Card firstDraw = new LightningBolt();
        Card secondDraw = new LightningBolt();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "RED");

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        harness.assertInGraveyard(player1, "Divination");
    }

    @Test
    @DisplayName("Restricted mana cannot pay for creature spells")
    void restrictedManaCannotCastCreature() {
        addCreatureReady(player1, new TempestTrapper());
        harness.setHand(player1, List.of(new TempestTrapper()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "BLUE");

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Exiling a random library card does not shuffle the library")
    void randomExileDoesNotShuffleLibrary() {
        addCreatureReady(player1, new TempestTrapper());
        Card libraryCard = new LightningBolt();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(libraryCard);
        assertThat(gameLogContains("shuffles their library")).isFalse();
    }

    @Test
    @DisplayName("The fourth spell does not trigger another random exile")
    void fourthSpellDoesNotExileAnotherCard() {
        addCreatureReady(player1, new TempestTrapper());
        Card firstCard = new LightningBolt();
        Card remainingCard = new LightningBolt();
        harness.setLibrary(player1, List.of(firstCard));
        harness.setHand(player1, List.of(
                new LightningBolt(), new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.setLibrary(player1, List.of(remainingCard));

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(firstCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
    }

    @Test
    @DisplayName("Spells cast before Tempest Trapper enters count toward the third spell")
    void earlierSpellsCountTowardThirdSpell() {
        Card libraryCard = new LightningBolt();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        addCreatureReady(player1, new TempestTrapper());

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(libraryCard);
    }

    @Test
    @DisplayName("An opponent's third spell does not trigger Tempest Trapper")
    void opponentThirdSpellDoesNotTrigger() {
        addCreatureReady(player1, new TempestTrapper());
        Card libraryCard = new LightningBolt();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    @DisplayName("The third spell still resolves when the library is empty")
    void emptyLibraryDoesNotPreventThirdSpellResolving() {
        addCreatureReady(player1, new TempestTrapper());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertLife(player2, 11);
    }

    @Test
    @DisplayName("A randomly exiled land can be played")
    void exiledLandCanBePlayed() {
        addCreatureReady(player1, new TempestTrapper());
        Card land = new Island();
        harness.setLibrary(player1, List.of(land));
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.castFromExile(player1, land.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(land);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(land.getId()));
    }

    @Test
    @DisplayName("An unplayed exiled card remains exiled after its permission expires")
    void freePlayPermissionExpiresAtEndOfTurn() {
        addCreatureReady(player1, new TempestTrapper());
        Card libraryCard = new LightningBolt();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(gd.exilePlayPermissions).containsKey(libraryCard.getId());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(libraryCard);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(libraryCard.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(libraryCard.getId());
    }
}
