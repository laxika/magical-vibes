package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DovinsAutomaton.class, DovinArchitectOfLaw.class})
class DovinsAutomatonTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+2 and vigilance while you control a Dovin planeswalker")
    void getsBonusWithDovin() {
        harness.addToBattlefield(player1, new DovinsAutomaton());
        harness.addToBattlefield(player1, new DovinArchitectOfLaw());

        Permanent automaton = findPermanent(player1, "Dovin's Automaton");
        assertThat(gqs.getEffectivePower(gd, automaton)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, automaton)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, automaton, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Does not get the bonus without a Dovin planeswalker")
    void noBonusWithoutDovin() {
        harness.addToBattlefield(player1, new DovinsAutomaton());

        Permanent automaton = findPermanent(player1, "Dovin's Automaton");
        assertThat(gqs.getEffectivePower(gd, automaton)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, automaton)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, automaton, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Does not count an opponent's Dovin planeswalker")
    void opponentDovinDoesNotCount() {
        harness.addToBattlefield(player1, new DovinsAutomaton());
        harness.addToBattlefield(player2, new DovinArchitectOfLaw());

        Permanent automaton = findPermanent(player1, "Dovin's Automaton");
        assertThat(gqs.getEffectivePower(gd, automaton)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, automaton)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, automaton, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Loses the bonus when the Dovin planeswalker leaves")
    void losesBonusWhenDovinLeaves() {
        harness.addToBattlefield(player1, new DovinsAutomaton());
        harness.addToBattlefield(player1, new DovinArchitectOfLaw());

        Permanent automaton = findPermanent(player1, "Dovin's Automaton");
        assertThat(gqs.getEffectivePower(gd, automaton)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, automaton, Keyword.VIGILANCE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard() instanceof DovinArchitectOfLaw);

        assertThat(gqs.getEffectivePower(gd, automaton)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, automaton)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, automaton, Keyword.VIGILANCE)).isFalse();
    }
}
