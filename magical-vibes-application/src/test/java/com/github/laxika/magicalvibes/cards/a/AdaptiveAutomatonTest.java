package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AdaptiveAutomatonTest extends BaseCardTest {

    private static Card createCreature(String name, String manaCost, int power, int toughness,
                                       CardColor color, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        card.setSubtypes(List.of(subtypes));
        return card;
    }

    private Permanent addAutomaton(com.github.laxika.magicalvibes.model.Player player, CardSubtype chosen) {
        Permanent perm = new Permanent(new AdaptiveAutomaton());
        perm.setChosenSubtype(chosen);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    @Test
    @DisplayName("Choosing a creature type as it enters sets chosenSubtype on the permanent")
    void choosingSubtypeSetsOnPermanent() {
        harness.setHand(player1, List.of(new AdaptiveAutomaton()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ELF");

        Permanent automaton = findPermanent(player1, "Adaptive Automaton");
        assertThat(automaton.getChosenSubtype()).isEqualTo(CardSubtype.ELF);
    }

    @Test
    @DisplayName("Adaptive Automaton is the chosen type in addition to its other types")
    void isTheChosenTypeItself() {
        Permanent automaton = addAutomaton(player1, CardSubtype.ELF);

        var bonus = gqs.computeStaticBonus(gd, automaton);
        assertThat(bonus.grantedSubtypes()).contains(CardSubtype.ELF);
        assertThat(automaton.getCard().getSubtypes()).contains(CardSubtype.CONSTRUCT);
    }

    @Test
    @DisplayName("Other creatures you control of the chosen type get +1/+1")
    void boostsOtherOwnCreaturesOfChosenType() {
        Card elf = createCreature("Llanowar Elves", "{G}", 1, 1, CardColor.GREEN, CardSubtype.ELF, CardSubtype.DRUID);
        harness.addToBattlefield(player1, elf);
        addAutomaton(player1, CardSubtype.ELF);

        var bonus = gqs.computeStaticBonus(gd, findPermanent(player1, "Llanowar Elves"));
        assertThat(bonus.power()).isEqualTo(1);
        assertThat(bonus.toughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Adaptive Automaton does not boost itself even though it is the chosen type")
    void doesNotBoostItself() {
        Permanent automaton = addAutomaton(player1, CardSubtype.CONSTRUCT);

        var bonus = gqs.computeStaticBonus(gd, automaton);
        assertThat(bonus.power()).isEqualTo(0);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("A second Adaptive Automaton naming the chosen type boosts the first one")
    void otherAutomatonBoostsThisOne() {
        Permanent first = addAutomaton(player1, CardSubtype.CONSTRUCT);
        addAutomaton(player1, CardSubtype.CONSTRUCT);

        var bonus = gqs.computeStaticBonus(gd, first);
        assertThat(bonus.power()).isEqualTo(1);
        assertThat(bonus.toughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Creatures of a different type are not boosted")
    void doesNotBoostDifferentType() {
        Card goblin = createCreature("Goblin Piker", "{1}{R}", 2, 1, CardColor.RED, CardSubtype.GOBLIN);
        harness.addToBattlefield(player1, goblin);
        addAutomaton(player1, CardSubtype.ELF);

        var bonus = gqs.computeStaticBonus(gd, findPermanent(player1, "Goblin Piker"));
        assertThat(bonus.power()).isEqualTo(0);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("Opponent's creatures of the chosen type are not boosted")
    void doesNotBoostOpponentCreatures() {
        Card elf = createCreature("Llanowar Elves", "{G}", 1, 1, CardColor.GREEN, CardSubtype.ELF);
        harness.addToBattlefield(player2, elf);
        addAutomaton(player1, CardSubtype.ELF);

        var bonus = gqs.computeStaticBonus(gd, findPermanent(player2, "Llanowar Elves"));
        assertThat(bonus.power()).isEqualTo(0);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("No boost or subtype grant before a creature type is chosen")
    void noEffectWithoutChoice() {
        Card elf = createCreature("Llanowar Elves", "{G}", 1, 1, CardColor.GREEN, CardSubtype.ELF);
        harness.addToBattlefield(player1, elf);
        harness.addToBattlefield(player1, new AdaptiveAutomaton());

        var bonus = gqs.computeStaticBonus(gd, findPermanent(player1, "Llanowar Elves"));
        assertThat(bonus.power()).isEqualTo(0);
        assertThat(bonus.toughness()).isEqualTo(0);
        assertThat(gqs.computeStaticBonus(gd, findPermanent(player1, "Adaptive Automaton")).grantedSubtypes())
                .isEmpty();
    }

    @Test
    @DisplayName("Boost ends when Adaptive Automaton leaves the battlefield")
    void boostRemovedWhenAutomatonLeaves() {
        Card elf = createCreature("Llanowar Elves", "{G}", 1, 1, CardColor.GREEN, CardSubtype.ELF);
        harness.addToBattlefield(player1, elf);
        Permanent automaton = addAutomaton(player1, CardSubtype.ELF);

        Permanent elfPerm = findPermanent(player1, "Llanowar Elves");
        assertThat(gqs.computeStaticBonus(gd, elfPerm).power()).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(automaton);

        assertThat(gqs.computeStaticBonus(gd, elfPerm).power()).isEqualTo(0);
    }
}
