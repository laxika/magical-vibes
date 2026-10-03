package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BorborygmosAndFblthp.class, Forest.class, Island.class})
class BorborygmosAndFblthpTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield draws, discards lands, and deals twice that much damage")
    void entersDrawsAndDamagesForDiscardedLands() {
        harness.setHand(player1, List.of(new BorborygmosAndFblthp(), new Forest(), new Island()));
        harness.setLibrary(player1, List.of(new BorborygmosAndFblthp()));
        addBorborygmosMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent borborygmos = findPermanent(player1, "Borborygmos and Fblthp");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);

        harness.handleXValueChosen(player1, 2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, borborygmos.getId());
        harness.passBothPriorities();

        assertThat(borborygmos.getMarkedDamage()).isEqualTo(4);
        harness.assertInHand(player1, "Borborygmos and Fblthp");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Island");
    }

    @Test
    @DisplayName("Attacking draws and uses the number of discarded lands for the reflexive damage")
    void attackTriggersDrawAndDiscardDamage() {
        Permanent borborygmos = addCreatureReady(player1, new BorborygmosAndFblthp());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new BorborygmosAndFblthp()));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(borborygmos)));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);

        harness.handleXValueChosen(player1, 1);
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, borborygmos.getId());
        harness.passBothPriorities();

        assertThat(borborygmos.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("The activated ability puts the source third from the top of its owner's library")
    void putsSelfThirdFromTop() {
        harness.addToBattlefield(player1, new BorborygmosAndFblthp());
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new BorborygmosAndFblthp()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(4);
        assertThat(library.get(2).getName()).isEqualTo("Borborygmos and Fblthp");
    }

    @Test
    @DisplayName("Choosing zero lands keeps the drawn card and creates no damage trigger")
    void mayDeclineToDiscard() {
        Permanent borborygmos = addCreatureReady(player1, new BorborygmosAndFblthp());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Island()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Island");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(borborygmos.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A hand with no lands still draws but cannot discard a nonland")
    void noLandsCreatesNoDiscardChoice() {
        Permanent borborygmos = addCreatureReady(player1, new BorborygmosAndFblthp());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new BorborygmosAndFblthp()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Borborygmos and Fblthp");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(borborygmos.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The land drawn by the trigger can be discarded for damage")
    void mayDiscardTheDrawnLand() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.castFromHand(player1, new BorborygmosAndFblthp(), "{2}{G}{U}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent borborygmos = findPermanent(player1, "Borborygmos and Fblthp");

        harness.handleXValueChosen(player1, 1);
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, borborygmos.getId());
        assertThat(borborygmos.getMarkedDamage()).isZero();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(borborygmos.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Reflexive damage can target an opposing creature and resolves after the source leaves")
    void damageResolvesAfterSourceLeaves() {
        addCreatureReady(player1, new BorborygmosAndFblthp());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BorborygmosAndFblthp());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Island(), new Forest(), new Island()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        assertThat(target.getMarkedDamage()).isZero();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Borborygmos and Fblthp");
        assertThat(target.getMarkedDamage()).isZero();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Forest");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    @DisplayName("With at most two cards in the library the source is placed on the bottom")
    void putsSelfAtBottomOfShortLibrary(int librarySize) {
        BorborygmosAndFblthp card = new BorborygmosAndFblthp();
        harness.addToBattlefield(player1, card);
        List<Card> originalLibrary = java.util.stream.IntStream.range(0, librarySize)
                .mapToObj(i -> (Card) new Forest()).toList();
        harness.setLibrary(player1, originalLibrary);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Borborygmos and Fblthp");
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(librarySize + 1);
        assertThat(library.subList(0, librarySize)).containsExactlyElementsOf(originalLibrary);
        assertThat(library.get(librarySize)).isSameAs(card);
    }

    private void addBorborygmosMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
