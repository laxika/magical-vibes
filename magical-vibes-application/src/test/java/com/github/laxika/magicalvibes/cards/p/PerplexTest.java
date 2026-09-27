package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LoreBroker;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Perplex.class, GrizzlyBears.class, Shock.class, LoreBroker.class})
class PerplexTest extends BaseCardTest {

    @Test
    void countersSpellWhenControllerDeclinesToDiscardHand() {
        GrizzlyBears bears = new GrizzlyBears();
        Shock cardInHand = new Shock();
        harness.setHand(player1, List.of(bears, cardInHand));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Perplex()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cardInHand);
    }

    @Test
    void acceptingDiscardsEntireHandAndSpellResolves() {
        GrizzlyBears bears = new GrizzlyBears();
        Shock cardInHand = new Shock();
        harness.setHand(player1, List.of(bears, cardInHand));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Perplex()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Shock");

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void cannotTargetAPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Perplex perplex = new Perplex();
        harness.setHand(player2, List.of(perplex));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(perplex);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    void acceptingWithEmptyHandPreventsCounter() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Perplex()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void transmuteSearchesForTheSameManaValue() {
        Perplex perplex = new Perplex();
        Perplex matchingCard = new Perplex();
        Shock differentManaValue = new Shock();
        harness.setHand(player1, List.of(perplex));
        harness.setLibrary(player1, List.of(matchingCard, differentManaValue));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(matchingCard);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Perplex");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(matchingCard);
    }

    @Test
    void transmuteShufflesWithoutFindingAMatchingCard() {
        Perplex perplex = new Perplex();
        Shock nonMatchingCard = new Shock();
        harness.setHand(player1, List.of(perplex));
        harness.setLibrary(player1, List.of(nonMatchingCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Perplex");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonMatchingCard);
    }

    @Test
    void transmuteUsesTheSourceManaValueAfterAnotherCardIsDiscardedInResponse() {
        Perplex perplex = new Perplex();
        Perplex matchingCard = new Perplex();
        Shock drawnAndDiscardedCard = new Shock();
        Permanent broker = addCreatureReady(player2, new LoreBroker());

        harness.setHand(player1, List.of(perplex));
        harness.setLibrary(player1, List.of(drawnAndDiscardedCard, matchingCard));
        harness.setHand(player2, List.of(new Shock()));
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(broker), null, null);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search)
                .as("the transmute search should still use Perplex's mana value after a response")
                .isNotNull();
        assertThat(search.params().cards()).containsExactly(matchingCard);
    }

    @Test
    void transmuteCanOnlyBeActivatedAtSorcerySpeed() {
        Perplex perplex = new Perplex();
        harness.setHand(player1, List.of(perplex));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(perplex);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }
}
