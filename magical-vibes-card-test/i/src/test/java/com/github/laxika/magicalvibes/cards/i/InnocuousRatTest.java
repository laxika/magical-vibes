package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({InnocuousRat.class, Shock.class, GrizzlyBears.class, Forest.class, Murder.class})
class InnocuousRatTest extends BaseCardTest {

    @Test
    void diesAndManifestsDread() {
        Card manifestedCard = new GrizzlyBears();
        Card graveyardCard = new Forest();
        Permanent rat = addCreatureReady(player1, new InnocuousRat());
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, rat.getId());
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.allCards()).containsExactly(manifestedCard, graveyardCard);

        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void manifestsOnlyCardInLibraryWithoutPuttingItIntoGraveyard() {
        Card onlyCard = new Forest();
        destroyRatAndResolveDeathTrigger(List.of(onlyCard));

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(onlyCard);
        harness.handleMultipleCardsChosen(player1, List.of(onlyCard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(permanent -> {
                    assertThat(permanent.getCard().getId()).isEqualTo(onlyCard.getId());
                    assertThat(permanent.isManifested()).isTrue();
                    assertThat(permanent.isFaceDown()).isTrue();
                });
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(onlyCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyLibraryDoesNotCreateCreatureOrRequireChoice() {
        destroyRatAndResolveDeathTrigger(List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canChooseSecondCardAndManifestLandButCannotTurnLandFaceUp() {
        Card firstCard = new InnocuousRat();
        Card secondCard = new Forest();
        Card untouchedCard = new Forest();
        destroyRatAndResolveDeathTrigger(List.of(firstCard, secondCard, untouchedCard));
        harness.handleMultipleCardsChosen(player1, List.of(secondCard.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstCard).doesNotContain(secondCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouchedCard);
        Permanent manifested = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(manifested.getCard().getId()).isEqualTo(secondCard.getId());
        assertThat(manifested.isManifested()).isTrue();
        assertThat(manifested.isFaceDown()).isTrue();
        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Face-down permanent is not a creature card");
        assertThat(manifested.isFaceDown()).isTrue();
    }

    @Test
    void manifestedCreatureCanTurnFaceUpForItsManaCost() {
        Card creature = new InnocuousRat();
        Card otherCard = new Forest();
        destroyRatAndResolveDeathTrigger(List.of(creature, otherCard));
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        Permanent manifested = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(manifested.isFaceDown()).isTrue();
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.turnFaceUp(player1, 0);

        assertThat(manifested.isFaceDown()).isFalse();
        assertThat(manifested.getCard().getId()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(otherCard).doesNotContain(creature);
    }

    @Test
    void manifestedRatHasNoDeathAbilityWhileFaceDown() {
        Card creature = new InnocuousRat();
        Card otherCard = new Forest();
        Card untouchedCard = new Forest();
        destroyRatAndResolveDeathTrigger(List.of(creature, otherCard, untouchedCard));
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        Permanent manifested = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, manifested.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouchedCard);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void destroyRatAndResolveDeathTrigger(List<Card> library) {
        Permanent rat = addCreatureReady(player1, new InnocuousRat());
        harness.setHand(player1, List.of(new Murder()));
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, rat.getId());
        resolveAllTriggers();
    }
}
