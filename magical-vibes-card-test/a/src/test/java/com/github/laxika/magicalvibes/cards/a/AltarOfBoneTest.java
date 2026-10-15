package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AltarOfBone.class, Aurochs.class, BalduvianBears.class, Counterspell.class, Plains.class})
class AltarOfBoneTest extends BaseCardTest {

    @Test
    @DisplayName("Casting sacrifices the chosen creature")
    void castingSacrificesCreature() {
        castWithSacrifice();

        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Balduvian Bears");
        harness.assertInGraveyard(player1, "Balduvian Bears");
    }

    @Test
    @DisplayName("Cannot cast without a creature to sacrifice")
    void cannotCastWithoutSacrifice() {
        harness.setHand(player1, List.of(new AltarOfBone()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Cannot cast when the chosen permanent is not a creature")
    void cannotSacrificeNonCreature() {
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.setHand(player1, List.of(new AltarOfBone()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");

        harness.assertOnBattlefield(player1, "Plains");
        harness.assertInHand(player1, "Altar of Bone");
    }

    @Test
    @DisplayName("Search offers only creature cards, regardless of mana value")
    void searchOffersOnlyCreatures() {
        castWithSacrifice();
        setupLibrary();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .hasSize(2)
                .anyMatch(BalduvianBears.class::isInstance)
                .anyMatch(Aurochs.class::isInstance)
                .noneMatch(Plains.class::isInstance);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();
        assertThat(search.params().shuffleAfterSelection()).isTrue();
    }

    @Test
    @DisplayName("Choosing a creature puts it into hand")
    void choosingPutsCreatureIntoHand() {
        castWithSacrifice();
        setupLibrary();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Balduvian Bears");
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(BalduvianBears.class::isInstance);
        harness.assertInGraveyard(player1, "Altar of Bone");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May fail to find a creature even when one is available")
    void mayFailToFindCreature() {
        castWithSacrifice();
        setupLibrary();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        harness.assertNotInHand(player1, "Balduvian Bears");
        harness.assertInGraveyard(player1, "Altar of Bone");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Does not prompt when the library has no creature cards")
    void noCreatureInLibrary() {
        castWithSacrifice();
        harness.setLibrary(player1, List.of(new Plains()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Altar of Bone");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature")
    void cannotSacrificeOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        harness.setHand(player1, List.of(new AltarOfBone()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("control");

        harness.assertOnBattlefield(player2, "Balduvian Bears");
        harness.assertInHand(player1, "Altar of Bone");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped creature with summoning sickness can pay the sacrifice cost")
    void canSacrificeTappedSummoningSickCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        sacrifice.tap();
        sacrifice.setSummoningSick(true);
        harness.setHand(player1, List.of(new AltarOfBone()));
        harness.setLibrary(player1, List.of(new Aurochs()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());

        harness.assertNotOnBattlefield(player1, "Balduvian Bears");
        harness.assertInGraveyard(player1, "Balduvian Bears");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Aurochs");
        harness.assertInGraveyard(player1, "Altar of Bone");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Resolves with an empty library without refunding the sacrificed creature")
    void emptyLibraryStillPaysSacrificeCost() {
        castWithSacrifice();
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Balduvian Bears");
        harness.assertInGraveyard(player1, "Altar of Bone");
        harness.assertNotOnBattlefield(player1, "Balduvian Bears");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Countering the spell does not refund the sacrificed creature or search the library")
    void counteredSpellKeepsSacrificePaid() {
        castWithSacrifice();
        Aurochs creatureInLibrary = new Aurochs();
        Plains landInLibrary = new Plains();
        harness.setLibrary(player1, List.of(creatureInLibrary, landInLibrary));

        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, gd.stack.getFirst().getCard().getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Altar of Bone");
        harness.assertInGraveyard(player1, "Balduvian Bears");
        harness.assertNotOnBattlefield(player1, "Balduvian Bears");
        harness.assertNotInHand(player1, "Aurochs");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creatureInLibrary, landInLibrary);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Searching takes exactly one creature from the caster's library into their hand")
    void searchUsesOnlyCastersLibraryAndHand() {
        castWithSacrifice();
        Aurochs chosenCreature = new Aurochs();
        BalduvianBears remainingCreature = new BalduvianBears();
        BalduvianBears opponentCreature = new BalduvianBears();
        harness.setLibrary(player1, List.of(chosenCreature, remainingCreature));
        harness.setLibrary(player2, List.of(opponentCreature));
        harness.setHand(player2, List.of());

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosenCreature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCreature);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCreature);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Altar of Bone");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castWithSacrifice() {
        Permanent sacrifice = addCreatureReady(player1, new BalduvianBears());

        harness.setHand(player1, List.of(new AltarOfBone()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new BalduvianBears(), new Aurochs(), new Plains()));
    }
}
