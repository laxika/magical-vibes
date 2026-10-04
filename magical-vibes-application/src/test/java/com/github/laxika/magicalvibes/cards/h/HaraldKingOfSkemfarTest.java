package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BeskirShieldmate;
import com.github.laxika.magicalvibes.cards.b.BindTheMonster;
import com.github.laxika.magicalvibes.cards.d.DoomskarOracle;
import com.github.laxika.magicalvibes.cards.j.JasperaSentinel;
import com.github.laxika.magicalvibes.cards.m.MaskedVandal;
import com.github.laxika.magicalvibes.cards.t.TyvarKell;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HaraldKingOfSkemfar.class, JasperaSentinel.class, BeskirShieldmate.class,
        TyvarKell.class, DoomskarOracle.class, BindTheMonster.class, MaskedVandal.class})
class HaraldKingOfSkemfarTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers Elf, Warrior, and Tyvar cards among the top five")
    void etbOffersMatchingCards() {
        Card elf = new JasperaSentinel();
        Card warrior = new BeskirShieldmate();
        Card tyvar = new TyvarKell();
        Card cleric = new DoomskarOracle();
        Card aura = new BindTheMonster();
        harness.setLibrary(player1, List.of(elf, warrior, tyvar, cleric, aura));

        castAndResolve();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactly(elf, warrior, tyvar, cleric, aura);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(elf.getId(), warrior.getId(), tyvar.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Choosing a matching card puts it into hand and randomly bottoms the rest")
    void choosingMatchingCardPutsItIntoHand() {
        Card elf = new JasperaSentinel();
        Card warrior = new BeskirShieldmate();
        Card tyvar = new TyvarKell();
        Card cleric = new DoomskarOracle();
        Card aura = new BindTheMonster();
        harness.setLibrary(player1, List.of(elf, warrior, tyvar, cleric, aura));

        castAndResolve();
        harness.handleMultipleCardsChosen(player1, List.of(tyvar.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(tyvar);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(elf, warrior, cleric, aura);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cards beyond the top five are not eligible")
    void onlyTopFiveAreEligible() {
        Card cleric = new DoomskarOracle();
        Card aura = new BindTheMonster();
        Card secondCleric = new DoomskarOracle();
        Card secondAura = new BindTheMonster();
        Card thirdCleric = new DoomskarOracle();
        Card elfBelowTopFive = new JasperaSentinel();
        harness.setLibrary(player1, List.of(cleric, aura, secondCleric, secondAura, thirdCleric, elfBelowTopFive));

        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(elfBelowTopFive);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 6))
                .containsExactlyInAnyOrder(cleric, aura, secondCleric, secondAura, thirdCleric);
    }

    @Test
    @DisplayName("Declining a matching card bottoms all five below untouched cards")
    void mayDeclineMatchingCard() {
        Card elf = new JasperaSentinel();
        Card warrior = new BeskirShieldmate();
        Card tyvar = new TyvarKell();
        Card cleric = new DoomskarOracle();
        Card aura = new BindTheMonster();
        Card untouched = new JasperaSentinel();
        harness.setLibrary(player1, List.of(elf, warrior, tyvar, cleric, aura, untouched));

        castAndResolve();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 6))
                .containsExactlyInAnyOrder(elf, warrior, tyvar, cleric, aura);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Choosing a Warrior preserves the untouched library above the remaining cards")
    void choosingWarriorBottomsOnlyLookedAtCards() {
        Card warrior = new BeskirShieldmate();
        Card elf = new JasperaSentinel();
        Card tyvar = new TyvarKell();
        Card cleric = new DoomskarOracle();
        Card aura = new BindTheMonster();
        Card untouched = new DoomskarOracle();
        harness.setLibrary(player1, List.of(warrior, elf, tyvar, cleric, aura, untouched));

        castAndResolve();
        harness.handleMultipleCardsChosen(player1, List.of(warrior.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(warrior);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 5))
                .containsExactlyInAnyOrder(elf, tyvar, cleric, aura);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Changeling is eligible in a library with fewer than five cards")
    void changelingIsEligibleInShortLibrary() {
        Card changeling = new MaskedVandal();
        Card cleric = new DoomskarOracle();
        harness.setLibrary(player1, List.of(changeling, cleric));

        castAndResolve();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactly(changeling, cleric);
        assertThat(choice.validCardIds()).containsExactly(changeling.getId());
        harness.handleMultipleCardsChosen(player1, List.of(changeling.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(changeling);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(cleric);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A single eligible card can still be declined")
    void mayDeclineOnlyCardInLibrary() {
        Card elf = new JasperaSentinel();
        harness.setLibrary(player1, List.of(elf));

        castAndResolve();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(elf);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library resolves without a choice or drawing a card")
    void emptyLibraryDoesNothing() {
        harness.setLibrary(player1, List.of());

        castAndResolve();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Menace prevents Harald from being blocked by one creature")
    void menaceRequiresTwoBlockers() {
        addCreatureReady(player1, new HaraldKingOfSkemfar());
        addCreatureReady(player2, new DoomskarOracle());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    @DisplayName("Two creatures can block Harald")
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new HaraldKingOfSkemfar());
        Permanent first = addCreatureReady(player2, new DoomskarOracle());
        Permanent second = addCreatureReady(player2, new DoomskarOracle());
        declareAttackers(player1, List.of(0));
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new HaraldKingOfSkemfar()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
