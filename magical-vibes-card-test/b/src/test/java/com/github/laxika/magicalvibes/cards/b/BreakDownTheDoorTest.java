package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GildedLotus;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BreakDownTheDoor.class, Forest.class, GildedLotus.class, GloriousAnthem.class, GrizzlyBears.class})
class BreakDownTheDoorTest extends BaseCardTest {

    @Test
    void exilesTargetArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GildedLotus());

        cast(0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(artifact.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(artifact.getCard().getId()));
    }

    @Test
    void exilesTargetEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        cast(1, enchantment.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(enchantment.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(enchantment.getCard().getId()));
    }

    @Test
    void artifactModeRejectsNonArtifactTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void manifestsDread() {
        Card manifestedCard = new GrizzlyBears();
        Card graveyardCard = new Forest();
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        prepareSpell();

        harness.castInstant(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(graveyardCard.getId()));
    }

    @Test
    void enchantmentModeRejectsArtifactTarget() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GildedLotus());
        prepareSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canManifestTheSecondCardEvenWhenItIsALand() {
        Card graveyardCard = new BreakDownTheDoor();
        Card manifestedCard = new Forest();
        Card untouchedCard = new Forest();
        harness.setLibrary(player1, List.of(graveyardCard, manifestedCard, untouchedCard));

        cast(2, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).anySatisfy(permanent -> {
            assertThat(permanent.getCard().getId()).isEqualTo(manifestedCard.getId());
            assertThat(permanent.isManifested()).isTrue();
            assertThat(permanent.isFaceDown()).isTrue();
        });
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(graveyardCard.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouchedCard);
        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void manifestsTheOnlyCardInLibrary() {
        Card manifestedCard = new Forest();
        harness.setLibrary(player1, List.of(manifestedCard));

        cast(2, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(manifestedCard.getId()));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyLibraryDoesNotRequireAChoice() {
        harness.setLibrary(player1, List.of());

        cast(2, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Break Down the Door");
        harness.assertLife(player1, 20);
    }

    @Test
    void manifestedCreatureCanBeTurnedFaceUpForItsManaCost() {
        Card manifestedCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(manifestedCard, new Forest()));

        cast(2, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.turnFaceUp(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).anySatisfy(permanent -> {
            assertThat(permanent.getCard().getId()).isEqualTo(manifestedCard.getId());
            assertThat(permanent.isFaceDown()).isFalse();
        });
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void artifactModeDoesNotManifestWhenItsTargetLeavesTheBattlefield() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GildedLotus());
        Card libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));

        cast(0, artifact.getId());
        gd.playerBattlefields.get(player2.getId()).remove(artifact);
        gd.playerGraveyards.get(player2.getId()).add(artifact.getCard());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Break Down the Door");
    }

    private void cast(int mode, java.util.UUID targetId) {
        prepareSpell();
        harness.castInstant(player1, 0, mode, targetId);
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new BreakDownTheDoor()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
