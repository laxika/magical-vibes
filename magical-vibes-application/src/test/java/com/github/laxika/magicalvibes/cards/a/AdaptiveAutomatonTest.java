package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AdaptiveAutomaton.class, LlanowarElves.class, GoblinPiker.class})
class AdaptiveAutomatonTest extends BaseCardTest {

    private Permanent addAutomaton(com.github.laxika.magicalvibes.model.Player player, CardSubtype chosen) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new AdaptiveAutomaton());
        perm.setChosenSubtype(chosen);
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

        assertThat(gqs.hasEffectiveSubtype(gd, automaton, CardSubtype.ELF)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, automaton, CardSubtype.CONSTRUCT)).isTrue();
    }

    @Test
    @DisplayName("Other creatures you control of the chosen type get +1/+1")
    void boostsOtherOwnCreaturesOfChosenType() {
        harness.addToBattlefield(player1, new LlanowarElves());
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
    @DisplayName("Two Automatons naming Elf boost each other through their added type")
    void automatonsBoostEachOtherThroughChosenType() {
        Permanent first = addAutomaton(player1, CardSubtype.ELF);
        Permanent second = addAutomaton(player1, CardSubtype.ELF);

        assertThat(gqs.computeStaticBonus(gd, first).power()).isEqualTo(1);
        assertThat(gqs.computeStaticBonus(gd, first).toughness()).isEqualTo(1);
        assertThat(gqs.computeStaticBonus(gd, second).power()).isEqualTo(1);
        assertThat(gqs.computeStaticBonus(gd, second).toughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature entering without being cast still gets a type choice")
    void choosesTypeWhenNotCast() {
        harness.addToBattlefield(player1, new LlanowarElves());
        Permanent automaton = harness.enterBattlefieldAndReturn(player1, new AdaptiveAutomaton());
        harness.handleListChoice(player1, "ELF");

        assertThat(automaton.getChosenSubtype()).isEqualTo(CardSubtype.ELF);
        assertThat(gqs.computeStaticBonus(gd, findPermanent(player1, "Llanowar Elves")).power())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Creatures of a different type are not boosted")
    void doesNotBoostDifferentType() {
        harness.addToBattlefield(player1, new GoblinPiker());
        addAutomaton(player1, CardSubtype.ELF);

        var bonus = gqs.computeStaticBonus(gd, findPermanent(player1, "Goblin Piker"));
        assertThat(bonus.power()).isEqualTo(0);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("Opponent's creatures of the chosen type are not boosted")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player2, new LlanowarElves());
        addAutomaton(player1, CardSubtype.ELF);

        var bonus = gqs.computeStaticBonus(gd, findPermanent(player2, "Llanowar Elves"));
        assertThat(bonus.power()).isEqualTo(0);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("No boost or subtype grant before a creature type is chosen")
    void noEffectWithoutChoice() {
        harness.addToBattlefield(player1, new LlanowarElves());
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
        harness.addToBattlefield(player1, new LlanowarElves());
        Permanent automaton = addAutomaton(player1, CardSubtype.ELF);

        Permanent elfPerm = findPermanent(player1, "Llanowar Elves");
        assertThat(gqs.computeStaticBonus(gd, elfPerm).power()).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(automaton);

        assertThat(gqs.computeStaticBonus(gd, elfPerm).power()).isEqualTo(0);
    }
}
