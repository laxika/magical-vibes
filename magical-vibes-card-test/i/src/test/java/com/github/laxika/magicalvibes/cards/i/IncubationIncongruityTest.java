package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IncubationIncongruity.class, Divination.class, Forest.class, GrizzlyBears.class,
        LlanowarElves.class, Plains.class, Shock.class})
class IncubationIncongruityTest extends BaseCardTest {

    @Test
    @DisplayName("Incubation may reveal a creature from the top five and bottoms the rest randomly")
    void incubationRevealsCreatureAndBottomsRest() {
        LlanowarElves creature = new LlanowarElves();
        Shock shock = new Shock();
        Plains plains = new Plains();
        Divination divination = new Divination();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(shock, creature, plains, divination, forest));
        harness.setHand(player1, List.of(new IncubationIncongruity()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).extracting(Card::getName)
                .containsExactlyInAnyOrder("Shock", "Llanowar Elves", "Plains", "Divination", "Forest");
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        harness.assertInHand(player1, "Llanowar Elves");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Shock", "Plains", "Divination", "Forest");
    }

    @Test
    @DisplayName("Incongruity exiles a creature and gives its controller a Frog Lizard")
    void incongruityExilesCreatureAndCreatesToken() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IncubationIncongruity()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castModalInstant(player1, 0, 1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        Permanent token = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Frog Lizard"))
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(3);
        assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.FROG, CardSubtype.LIZARD);
    }

    @Test
    @DisplayName("Incongruity cannot target a noncreature permanent")
    void incongruityCannotTargetNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new IncubationIncongruity()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void incubationCanDeclineCreatureAndPreservesUnseenTopCard() {
        LlanowarElves creature = new LlanowarElves();
        Forest unseen = new Forest();
        List<Card> viewed = List.of(creature, new Shock(), new Plains(), new Divination(), new Forest());
        harness.setLibrary(player1, List.of(viewed.get(0), viewed.get(1), viewed.get(2),
                viewed.get(3), viewed.get(4), unseen));
        harness.setHand(player1, List.of(new IncubationIncongruity()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(unseen);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 6))
                .containsExactlyInAnyOrderElementsOf(viewed);
    }

    @Test
    void incubationWithNoCreatureBottomsOnlyViewedCards() {
        LlanowarElves unseen = new LlanowarElves();
        List<Card> viewed = List.of(new Shock(), new Plains(), new Divination(), new Forest(), new Plains());
        harness.setLibrary(player1, List.of(viewed.get(0), viewed.get(1), viewed.get(2),
                viewed.get(3), viewed.get(4), unseen));
        harness.setHand(player1, List.of(new IncubationIncongruity()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(unseen);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 6))
                .containsExactlyInAnyOrderElementsOf(viewed);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void incubationLooksAtAllCardsInShortLibraryAndChoosesOnlyOneCreature() {
        LlanowarElves chosen = new LlanowarElves();
        GrizzlyBears remaining = new GrizzlyBears();
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(chosen, remaining, land));
        harness.setHand(player1, List.of(new IncubationIncongruity()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(remaining, land);
    }

    @Test
    void incubationWithEmptyLibraryDoesNotRequireAChoice() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new IncubationIncongruity()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void incongruityCanBeCastDuringOpponentsTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IncubationIncongruity()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.castModalInstant(player1, 0, 1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        harness.assertOnBattlefield(player2, "Frog Lizard");
    }

    @Test
    void incubationCannotBeCastDuringOpponentsTurn() {
        harness.setHand(player1, List.of(new IncubationIncongruity()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void incongruityCreatesNoTokenWhenTargetDiesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IncubationIncongruity()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castModalInstant(player1, 0, 1, List.of(target.getId()));
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Frog Lizard");
        harness.assertNotOnBattlefield(player1, "Frog Lizard");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void incongruityCanExileOwnCreatureAndCreatesTokenForCaster() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new IncubationIncongruity()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castModalInstant(player1, 0, 1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target.getCard());
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Frog Lizard");
        harness.assertNotOnBattlefield(player2, "Frog Lizard");
    }
}
