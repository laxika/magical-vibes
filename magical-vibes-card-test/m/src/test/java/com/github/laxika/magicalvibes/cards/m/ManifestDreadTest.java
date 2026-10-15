package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BashfulBeastie;
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

@CardUsed({ManifestDread.class, Forest.class, BashfulBeastie.class})
class ManifestDreadTest extends BaseCardTest {

    @Test
    void manifestsOneOfTopTwoAndPutsTheOtherIntoGraveyard() {
        Card manifestedCard = new BashfulBeastie();
        Card graveyardCard = new Forest();
        harness.setHand(player1, List.of(new ManifestDread()));
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(manifestedCard, graveyardCard);

        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void canManifestTheSecondCardEvenWhenItIsALand() {
        Card firstCard = new BashfulBeastie();
        Card secondCard = new Forest();
        Card untouchedCard = new ManifestDread();
        harness.setHand(player1, List.of(new ManifestDread()));
        harness.setLibrary(player1, List.of(firstCard, secondCard, untouchedCard));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(secondCard.getId()));

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(manifested.getCard()).isSameAs(secondCard);
        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(manifested.isManifested()).isTrue();
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstCard).doesNotContain(secondCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouchedCard);
        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(manifested.isFaceDown()).isTrue();
    }

    @Test
    void manifestsTheOnlyCardWithoutPuttingItIntoTheGraveyard() {
        Card onlyCard = new BashfulBeastie();
        harness.setHand(player1, List.of(new ManifestDread()));
        harness.setLibrary(player1, List.of(onlyCard));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(onlyCard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getCard()).isSameAs(onlyCard);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isManifested()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(onlyCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void emptyLibraryDoesNothingAndDoesNotRequireAChoice() {
        harness.setHand(player1, List.of(new ManifestDread()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Manifest Dread");
    }

    @Test
    void creatureCanTurnFaceUpForItsManaCostWithoutUsingTheStack() {
        Card creature = new BashfulBeastie();
        harness.setHand(player1, List.of(new ManifestDread()));
        harness.setLibrary(player1, List.of(creature, new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(manifested.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, 0);

        assertThat(manifested.isFaceDown()).isFalse();
        assertThat(manifested.isManifested()).isFalse();
        assertThat(manifested.getCard()).isSameAs(creature);
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }
}
