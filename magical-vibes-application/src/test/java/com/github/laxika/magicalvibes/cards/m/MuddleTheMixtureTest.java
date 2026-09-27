package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CompulsiveResearch;
import com.github.laxika.magicalvibes.cards.d.Darkblast;
import com.github.laxika.magicalvibes.cards.d.DizzySpell;
import com.github.laxika.magicalvibes.cards.l.LoreBroker;
import com.github.laxika.magicalvibes.cards.w.Watchwolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MuddleTheMixture.class, DizzySpell.class, CompulsiveResearch.class, Watchwolf.class,
        Darkblast.class, LoreBroker.class})
class MuddleTheMixtureTest extends BaseCardTest {

    @Test
    void countersInstantSpell() {
        DizzySpell dizzySpell = new DizzySpell();
        Permanent targetCreature = harness.addToBattlefieldAndReturn(player1, new Watchwolf());
        MuddleTheMixture muddle = new MuddleTheMixture();
        harness.setHand(player1, List.of(dizzySpell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player2, List.of(muddle));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.setLife(player2, 20);
        harness.castInstant(player1, 0, targetCreature.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, dizzySpell.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dizzy Spell");
        harness.assertLife(player2, 20);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    void countersSorcerySpell() {
        CompulsiveResearch compulsiveResearch = new CompulsiveResearch();
        Darkblast firstLibraryCard = new Darkblast();
        Darkblast secondLibraryCard = new Darkblast();
        harness.setHand(player1, List.of(compulsiveResearch));
        harness.setLibrary(player1, List.of(firstLibraryCard, secondLibraryCard));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setHand(player2, List.of(new MuddleTheMixture()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, compulsiveResearch.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Compulsive Research");
        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().playerDecks.get(player1.getId()))
                .containsExactly(firstLibraryCard, secondLibraryCard);
    }

    @Test
    void cannotTargetCreatureSpell() {
        Watchwolf watchwolf = new Watchwolf();
        MuddleTheMixture muddle = new MuddleTheMixture();
        harness.setHand(player1, List.of(watchwolf));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player2, List.of(muddle));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, watchwolf.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(muddle);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(2);
    }

    @Test
    void transmuteSearchesForTheSameManaValue() {
        MuddleTheMixture muddle = new MuddleTheMixture();
        Watchwolf matchingCard = new Watchwolf();
        Darkblast differentManaValue = new Darkblast();
        harness.setHand(player1, List.of(muddle));
        harness.setLibrary(player1, List.of(matchingCard, differentManaValue));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(matchingCard);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Muddle the Mixture");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(matchingCard);
    }

    @Test
    void transmuteShufflesWithoutFindingAMatchingCard() {
        MuddleTheMixture muddle = new MuddleTheMixture();
        Darkblast nonMatchingCard = new Darkblast();
        harness.setHand(player1, List.of(muddle));
        harness.setLibrary(player1, List.of(nonMatchingCard));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Muddle the Mixture");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonMatchingCard);
    }

    @Test
    void transmuteCanOnlyBeActivatedAtSorcerySpeed() {
        MuddleTheMixture muddle = new MuddleTheMixture();
        harness.setHand(player1, List.of(muddle));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(muddle);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    void transmuteUsesTheSourceManaValueAfterAnotherCardIsDiscardedInResponse() {
        MuddleTheMixture muddle = new MuddleTheMixture();
        Watchwolf matchingCard = new Watchwolf();
        Darkblast drawnAndDiscardedCard = new Darkblast();
        LoreBroker loreBroker = new LoreBroker();
        Permanent broker = addCreatureReady(player2, loreBroker);

        harness.setHand(player1, List.of(muddle));
        harness.setLibrary(player1, List.of(drawnAndDiscardedCard, matchingCard));
        harness.setHand(player2, List.of(new Darkblast()));
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(broker), null, null);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search)
                .as("the transmute search should still use Muddle the Mixture's mana value after a response")
                .isNotNull();
        assertThat(search.params().cards()).containsExactly(matchingCard);
    }
}
