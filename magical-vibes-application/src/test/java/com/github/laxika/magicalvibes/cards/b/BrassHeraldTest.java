package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class BrassHeraldTest extends BaseCardTest {

    private static Card createCreature(String name, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(subtypes));
        return card;
    }

    private static Card createCard(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        return card;
    }

    private void finishAnyReorder() {
        var reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        if (reorder != null) {
            harness.getGameService().handleLibraryCardsReordered(gd, player1,
                    IntStream.range(0, reorder.cards().size()).boxed().toList());
        }
    }

    private void castHeraldAndChoose(String subtype) {
        harness.setHand(player1, List.of(new BrassHerald()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();          // resolve Brass Herald -> subtype choice pends
        harness.handleListChoice(player1, subtype); // choose type -> reveal trigger queued
        harness.passBothPriorities();          // resolve the reveal trigger
    }

    // ===== ETB reveal =====

    @Test
    @DisplayName("Creature cards of the chosen type go to hand, the rest to the bottom")
    void revealPutsChosenTypeCreaturesInHand() {
        Card bear1 = createCreature("Grizzly Bears", CardSubtype.BEAR);
        Card bear2 = createCreature("Runeclaw Bear", CardSubtype.BEAR);
        Card forest = createCard("Forest", CardType.LAND);
        Card shock = createCard("Shock", CardType.INSTANT);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(bear1, bear2, forest, shock));

        castHeraldAndChoose("BEAR");
        finishAnyReorder();

        assertThat(gd.playerHands.get(player1.getId())).contains(bear1, bear2);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest, shock);
        // Non-matching cards were put on the bottom of the library.
        assertThat(deck).contains(forest, shock);
    }

    @Test
    @DisplayName("A creature of a different type is not put into hand")
    void revealSkipsOtherCreatureTypes() {
        Card elf = createCreature("Llanowar Elves", CardSubtype.ELF);
        Card goblin = createCreature("Goblin Piker", CardSubtype.GOBLIN);
        Card bird = createCreature("Storm Crow", CardSubtype.BIRD);
        Card land = createCard("Forest", CardType.LAND);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(elf, goblin, bird, land));

        castHeraldAndChoose("ELF");
        finishAnyReorder();

        assertThat(gd.playerHands.get(player1.getId())).contains(elf);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(goblin, bird, land);
    }

    @Test
    @DisplayName("Changeling creature cards count as the chosen type")
    void revealChangelingCountsAsChosenType() {
        Card changeling = createCreature("Woodland Changeling");
        changeling.setKeywords(Set.of(Keyword.CHANGELING));
        Card land = createCard("Forest", CardType.LAND);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(changeling, land));

        castHeraldAndChoose("ELF");
        finishAnyReorder();

        assertThat(gd.playerHands.get(player1.getId())).contains(changeling);
    }

    // ===== Static +1/+1 boost =====

    @Test
    @DisplayName("Creatures you control of the chosen type get +1/+1")
    void boostsOwnCreaturesOfChosenType() {
        Card elf = createCreature("Llanowar Elves", CardSubtype.ELF);
        harness.addToBattlefield(player1, elf);

        Permanent herald = new Permanent(new BrassHerald());
        herald.setChosenSubtype(CardSubtype.ELF);
        gd.playerBattlefields.get(player1.getId()).add(herald);

        Permanent elfPerm = findPermanent(player1, "Llanowar Elves");
        var bonus = gqs.computeStaticBonus(gd, elfPerm);
        assertThat(bonus.power()).isEqualTo(1);
        assertThat(bonus.toughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent's creatures of the chosen type also get +1/+1")
    void boostsOpponentCreaturesOfChosenType() {
        Card elf = createCreature("Llanowar Elves", CardSubtype.ELF);
        harness.addToBattlefield(player2, elf);

        Permanent herald = new Permanent(new BrassHerald());
        herald.setChosenSubtype(CardSubtype.ELF);
        gd.playerBattlefields.get(player1.getId()).add(herald);

        Permanent elfPerm = findPermanent(player2, "Llanowar Elves");
        var bonus = gqs.computeStaticBonus(gd, elfPerm);
        assertThat(bonus.power()).isEqualTo(1);
        assertThat(bonus.toughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Creatures of a different type are not boosted")
    void doesNotBoostOtherTypes() {
        Card goblin = createCreature("Goblin Piker", CardSubtype.GOBLIN);
        harness.addToBattlefield(player1, goblin);

        Permanent herald = new Permanent(new BrassHerald());
        herald.setChosenSubtype(CardSubtype.ELF);
        gd.playerBattlefields.get(player1.getId()).add(herald);

        Permanent goblinPerm = findPermanent(player1, "Goblin Piker");
        var bonus = gqs.computeStaticBonus(gd, goblinPerm);
        assertThat(bonus.power()).isEqualTo(0);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("Brass Herald boosts itself when it chooses its own creature type")
    void boostsItselfWhenChoosingGolem() {
        Permanent herald = new Permanent(new BrassHerald());
        herald.setChosenSubtype(CardSubtype.GOLEM);
        gd.playerBattlefields.get(player1.getId()).add(herald);

        var bonus = gqs.computeStaticBonus(gd, herald);
        assertThat(bonus.power()).isEqualTo(1);
        assertThat(bonus.toughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost disappears when Brass Herald leaves the battlefield")
    void boostRemovedWhenHeraldLeaves() {
        Card elf = createCreature("Llanowar Elves", CardSubtype.ELF);
        harness.addToBattlefield(player1, elf);

        Permanent herald = new Permanent(new BrassHerald());
        herald.setChosenSubtype(CardSubtype.ELF);
        gd.playerBattlefields.get(player1.getId()).add(herald);

        Permanent elfPerm = findPermanent(player1, "Llanowar Elves");
        assertThat(gqs.computeStaticBonus(gd, elfPerm).power()).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(herald);
        assertThat(gqs.computeStaticBonus(gd, elfPerm).power()).isEqualTo(0);
    }
}
