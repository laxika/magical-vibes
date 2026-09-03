package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
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

@CardUsed({AltarOfBone.class, Aurochs.class, BalduvianBears.class, Plains.class})
class AltarOfBoneTest extends BaseCardTest {

    @Test
    @DisplayName("Casting sacrifices the chosen creature")
    void castingSacrificesCreature() {
        castWithSacrifice();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof BalduvianBears);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(BalduvianBears.class::isInstance);
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

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof Plains);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(AltarOfBone.class::isInstance);
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

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(BalduvianBears.class::isInstance);
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(BalduvianBears.class::isInstance);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(AltarOfBone.class::isInstance);
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
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(BalduvianBears.class::isInstance);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(AltarOfBone.class::isInstance);
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
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(AltarOfBone.class::isInstance);
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
