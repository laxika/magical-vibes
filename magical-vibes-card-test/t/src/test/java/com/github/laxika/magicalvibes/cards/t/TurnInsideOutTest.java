package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TurnInsideOut.class, Shock.class, GrizzlyBears.class, Forest.class})
class TurnInsideOutTest extends BaseCardTest {

    @Test
    void boostsTargetAndManifestsDreadWhenItDiesThisTurn() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Card manifestedCard = new GrizzlyBears();
        Card graveyardCard = new Forest();
        harness.setHand(player1, List.of(new TurnInsideOut(), new Shock()));
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);

        harness.castAndResolveInstant(player1, 0, target.getId());
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
    }

    @Test
    void manifestsForSpellControllerWhenOpponentsCreatureDies() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Card manifestedCard = new Forest();
        Card graveyardCard = new GrizzlyBears();
        Card opponentsLibraryCard = new Forest();
        harness.setHand(player1, List.of(new TurnInsideOut(), new Shock()));
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        harness.setLibrary(player2, List.of(opponentsLibraryCard));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.allCards()).containsExactly(manifestedCard, graveyardCard);
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(manifestedCard.getId()))
                .findFirst().orElseThrow();
        assertThat(manifested.isManifested()).isTrue();
        assertThat(manifested.getEffectivePower()).isEqualTo(2);
        assertThat(manifested.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsLibraryCard);
    }

    @Test
    void doesNotManifestWhenTargetDiesBeforeSpellResolves() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Card libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new TurnInsideOut(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target.getCard());
    }

    @Test
    void boostAndDeathTriggerExpireAtEndOfTurn() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Card libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new TurnInsideOut(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(target.getEffectivePower()).isEqualTo(5);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.getEffectivePower()).isEqualTo(2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target.getCard());
    }

    @Test
    void manifestsOnlyCardInLibraryWithoutPuttingAnotherCardInGraveyard() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Card libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new TurnInsideOut(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(libraryCard);
        harness.handleMultipleCardsChosen(player1, List.of(libraryCard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(libraryCard.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(libraryCard);
    }
}
