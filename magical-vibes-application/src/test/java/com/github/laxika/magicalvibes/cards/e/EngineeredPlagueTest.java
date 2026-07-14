package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EngineeredPlagueTest extends BaseCardTest {

    private static Card createCreature(String name, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(subtypes));
        return card;
    }

    private Permanent addPlague(CardSubtype chosen) {
        Permanent plague = new Permanent(new EngineeredPlague());
        plague.setChosenSubtype(chosen);
        gd.playerBattlefields.get(player1.getId()).add(plague);
        return plague;
    }

    @Test
    @DisplayName("Choosing a creature type on enter, then all creatures of that type get -1/-1")
    void choosesTypeOnEnter() {
        harness.setHand(player1, List.of(new EngineeredPlague()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();          // resolve -> subtype choice pends
        harness.handleListChoice(player1, "GOBLIN");

        Card goblin = createCreature("Goblin Piker", CardSubtype.GOBLIN);
        harness.addToBattlefield(player1, goblin);
        Permanent goblinPerm = findPermanent(player1, "Goblin Piker");

        var bonus = gqs.computeStaticBonus(gd, goblinPerm);
        assertThat(bonus.power()).isEqualTo(-1);
        assertThat(bonus.toughness()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Own creatures of the chosen type get -1/-1")
    void weakensOwnCreaturesOfChosenType() {
        Card goblin = createCreature("Goblin Piker", CardSubtype.GOBLIN);
        harness.addToBattlefield(player1, goblin);
        addPlague(CardSubtype.GOBLIN);

        Permanent goblinPerm = findPermanent(player1, "Goblin Piker");
        var bonus = gqs.computeStaticBonus(gd, goblinPerm);
        assertThat(bonus.power()).isEqualTo(-1);
        assertThat(bonus.toughness()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Opponent's creatures of the chosen type also get -1/-1")
    void weakensOpponentCreaturesOfChosenType() {
        Card goblin = createCreature("Goblin Piker", CardSubtype.GOBLIN);
        harness.addToBattlefield(player2, goblin);
        addPlague(CardSubtype.GOBLIN);

        Permanent goblinPerm = findPermanent(player2, "Goblin Piker");
        var bonus = gqs.computeStaticBonus(gd, goblinPerm);
        assertThat(bonus.power()).isEqualTo(-1);
        assertThat(bonus.toughness()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Creatures of a different type are not affected")
    void doesNotAffectOtherTypes() {
        Card elf = createCreature("Llanowar Elves", CardSubtype.ELF);
        harness.addToBattlefield(player1, elf);
        addPlague(CardSubtype.GOBLIN);

        Permanent elfPerm = findPermanent(player1, "Llanowar Elves");
        var bonus = gqs.computeStaticBonus(gd, elfPerm);
        assertThat(bonus.power()).isEqualTo(0);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("The -1/-1 disappears when Engineered Plague leaves the battlefield")
    void effectRemovedWhenPlagueLeaves() {
        Card goblin = createCreature("Goblin Piker", CardSubtype.GOBLIN);
        harness.addToBattlefield(player1, goblin);
        Permanent plague = addPlague(CardSubtype.GOBLIN);

        Permanent goblinPerm = findPermanent(player1, "Goblin Piker");
        assertThat(gqs.computeStaticBonus(gd, goblinPerm).power()).isEqualTo(-1);

        gd.playerBattlefields.get(player1.getId()).remove(plague);
        assertThat(gqs.computeStaticBonus(gd, goblinPerm).power()).isEqualTo(0);
    }
}
