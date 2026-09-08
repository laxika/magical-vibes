package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VampireOfTheDireMoon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MarkovCrusaderTest extends BaseCardTest {

    @Test
    @DisplayName("Markov Crusader has haste when you control another Vampire")
    void hasHasteWhenControllerControlsAnotherVampire() {
        harness.addToBattlefield(player1, new MarkovCrusader());
        harness.addToBattlefield(player1, new VampireOfTheDireMoon());

        Permanent crusader = findPermanent(player1, "Markov Crusader");

        assertThat(gqs.hasKeyword(gd, crusader, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Markov Crusader does not have haste without another Vampire")
    void noHasteWithoutAnotherVampire() {
        harness.addToBattlefield(player1, new MarkovCrusader());

        Permanent crusader = findPermanent(player1, "Markov Crusader");

        assertThat(gqs.hasKeyword(gd, crusader, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("A non-Vampire does not grant Markov Crusader haste")
    void noHasteWithNonVampire() {
        harness.addToBattlefield(player1, new MarkovCrusader());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent crusader = findPermanent(player1, "Markov Crusader");

        assertThat(gqs.hasKeyword(gd, crusader, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Markov Crusader loses haste when the other Vampire leaves")
    void losesHasteWhenOtherVampireLeaves() {
        harness.addToBattlefield(player1, new MarkovCrusader());
        harness.addToBattlefield(player1, new VampireOfTheDireMoon());

        Permanent crusader = findPermanent(player1, "Markov Crusader");
        Permanent vampire = findPermanent(player1, "Vampire of the Dire Moon");

        assertThat(gqs.hasKeyword(gd, crusader, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(vampire);

        assertThat(gqs.hasKeyword(gd, crusader, Keyword.HASTE)).isFalse();
    }
}
