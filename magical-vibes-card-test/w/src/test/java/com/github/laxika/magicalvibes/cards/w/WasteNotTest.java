package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.Distress;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LibraryOfLeng;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.cards.s.Sift;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WasteNot.class, Distress.class, GrizzlyBears.class, LibraryOfLeng.class, MindRot.class, Sift.class, Swamp.class})
class WasteNotTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent discarding a creature card creates a 2/2 black Zombie token")
    void creatureDiscardCreatesZombie() {
        harness.addToBattlefield(player1, new WasteNot());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        var zombies = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Zombie"))
                .toList();
        assertThat(zombies).hasSize(1);
        assertThat(zombies.getFirst().getCard().getPower()).isEqualTo(2);
        assertThat(zombies.getFirst().getCard().getToughness()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Opponent discarding a noncreature, nonland card draws its controller a card")
    void noncreatureNonlandDiscardDrawsCard() {
        harness.addToBattlefield(player1, new WasteNot());
        harness.setHand(player2, new ArrayList<>(List.of(new Sift())));
        harness.setLibrary(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Zombie");
    }

    @Test
    @DisplayName("Opponent discarding a land card adds {B}{B} to its controller's mana pool")
    void landDiscardAddsBlackMana() {
        harness.addToBattlefield(player1, new WasteNot());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setLibrary(player2, new ArrayList<>(List.of(new Swamp(), new Swamp(), new Swamp())));
        harness.setHand(player2, List.of(new Sift()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player2, 0, 0);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Zombie");
    }

    @Test
    @DisplayName("Waste Not does not trigger when its own controller discards")
    void doesNotTriggerOnControllerDiscard() {
        harness.addToBattlefield(player1, new WasteNot());
        harness.setLibrary(player1, new ArrayList<>(List.of(new Swamp(), new Swamp(), new Swamp())));
        harness.setHand(player1, List.of(new Sift()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        harness.assertNotOnBattlefield(player1, "Zombie");
    }

    @Test
    @DisplayName("Each discarded land creates a separate mana trigger that uses the stack")
    void twoDiscardedLandsProduceFourMana() {
        harness.addToBattlefield(player1, new WasteNot());
        harness.setHand(player2, List.of(new Swamp(), new Swamp()));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player1, "Zombie");
    }

    @Test
    @DisplayName("Mixed creature and noncreature discards each produce their appropriate reward")
    void mixedDiscardsCreateZombieAndDraw() {
        harness.addToBattlefield(player1, new WasteNot());
        harness.setHand(player2, List.of(new GrizzlyBears(), new Sift()));
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Zombie"))).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Two copies of Waste Not each trigger for an opponent's creature discard")
    void multipleCopiesEachCreateZombie() {
        harness.addToBattlefield(player1, new WasteNot());
        harness.addToBattlefield(player1, new WasteNot());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Zombie"))).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Discarding the controller's creature and noncreature cards gives no rewards")
    void ownMixedDiscardsDoNotTrigger() {
        harness.addToBattlefield(player1, new WasteNot());
        harness.setHand(player1, List.of(new MindRot(), new GrizzlyBears(), new Sift()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        harness.assertNotOnBattlefield(player1, "Zombie");
    }

    @Test
    @DisplayName("An unrevealed creature discarded to the library does not trigger Waste Not")
    void hiddenCreatureDiscardDoesNotTrigger() {
        assertHiddenDiscardDoesNotTrigger(new GrizzlyBears());
    }

    @Test
    @DisplayName("An unrevealed land discarded to the library does not trigger Waste Not")
    void hiddenLandDiscardDoesNotTrigger() {
        assertHiddenDiscardDoesNotTrigger(new Swamp());
    }

    @Test
    @DisplayName("An unrevealed noncreature nonland discarded to the library does not trigger Waste Not")
    void hiddenNoncreatureNonlandDiscardDoesNotTrigger() {
        assertHiddenDiscardDoesNotTrigger(new Sift());
    }

    private void assertHiddenDiscardDoesNotTrigger(Card discardedCard) {
        harness.addToBattlefield(player1, new WasteNot());
        harness.addToBattlefield(player2, new LibraryOfLeng());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Sift(), discardedCard));
        harness.setLibrary(player2, List.of(new Swamp(), new Swamp(), new Swamp()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player2, 0, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(discardedCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(discardedCard);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        harness.assertNotOnBattlefield(player1, "Zombie");
    }
}
