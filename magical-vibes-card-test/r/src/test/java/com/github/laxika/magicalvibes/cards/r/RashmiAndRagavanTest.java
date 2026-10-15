package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.l.LovestruckBeast;
import com.github.laxika.magicalvibes.cards.h.HeartsDesire;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RashmiAndRagavan.class, Forest.class, GrizzlyBears.class, LlanowarElves.class,
        Ornithopter.class, Shock.class, LovestruckBeast.class, HeartsDesire.class})
class RashmiAndRagavanTest extends BaseCardTest {

    @Test
    @DisplayName("The first spell exiles an opponent's top card and creates a Treasure")
    void firstSpellExilesTopCardAndCreatesTreasure() {
        setupRashmiAndRagavan();
        Card topCard = new Ornithopter();
        harness.setLibrary(player2, List.of(topCard, new Forest()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(topCard);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("An eligible exiled spell can be cast for free")
    void eligibleExiledSpellCanBeCastForFree() {
        setupRashmiAndRagavan();
        harness.addToBattlefield(player1, new Ornithopter());
        Card topCard = new LlanowarElves();
        harness.setLibrary(player2, List.of(topCard, new Forest()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(topCard);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("The trigger can target only an opponent")
    void triggerCannotTargetController() {
        setupRashmiAndRagavan();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("The trigger does not fire on your first spell during an opponent's turn")
    void triggerOnlyFiresDuringControllerTurn() {
        setupRashmiAndRagavan();
        Card topCard = new LlanowarElves();
        harness.setLibrary(player2, List.of(topCard, new Forest()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    void decliningFreeCastAllowsPaidCastLaterThisTurn() {
        setupRashmiAndRagavan();
        harness.addToBattlefield(player1, new Ornithopter());
        Card topCard = new LlanowarElves();
        harness.setLibrary(player2, List.of(topCard, new Forest()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(topCard);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void equalManaValueIsNotFreeButCanBeCastForMana() {
        setupRashmiAndRagavan();
        Card topCard = new LlanowarElves();
        harness.setLibrary(player2, List.of(topCard, new Forest()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(topCard);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    void emptyLibraryStillCreatesTreasure() {
        setupRashmiAndRagavan();
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void exiledLandCannotBePlayed() {
        setupRashmiAndRagavan();
        Card topCard = new Forest();
        harness.setLibrary(player2, List.of(topCard));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(topCard);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void secondSpellDoesNotTriggerAgain() {
        setupRashmiAndRagavan();
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2);
    }

    @Test
    void adventureIsEligibleEvenWhenCreatureManaValueIsTooHigh() {
        setupRashmiAndRagavan();
        harness.addToBattlefield(player1, new Ornithopter());
        Card topCard = new LovestruckBeast();
        harness.setLibrary(player2, List.of(topCard, new Forest()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(topCard);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
    }
    @Test
    void paidCreatureCastMustWaitForOriginalSpellToResolve() {
        setupRashmiAndRagavan();
        Card topCard = new LlanowarElves();
        harness.setLibrary(player2, List.of(topCard, new Forest()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    void enteringAfterFirstSpellDoesNotMakeSecondSpellTrigger() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new RashmiAndRagavan(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rashmi and Ragavan");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }
    private void setupRashmiAndRagavan() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new RashmiAndRagavan());
    }
}
