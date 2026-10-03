package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.StoneforgeMasterwork;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EldraziAggressor.class, Ornithopter.class, GrizzlyBears.class, StoneforgeMasterwork.class})
class EldraziAggressorTest extends BaseCardTest {

    @Test
    @DisplayName("Does not have haste without another colorless creature")
    void noHasteAlone() {
        Permanent aggressor = addCreatureReady(player1, new EldraziAggressor());

        assertThat(gqs.hasKeyword(gd, aggressor, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Has haste while controlling another colorless creature")
    void hasHasteWithAnotherColorlessCreature() {
        Permanent aggressor = addCreatureReady(player1, new EldraziAggressor());
        addCreatureReady(player1, new Ornithopter());

        assertThat(gqs.hasKeyword(gd, aggressor, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("A colored creature or an opponent's colorless creature does not grant haste")
    void unrelatedCreaturesDoNotGrantHaste() {
        Permanent aggressor = addCreatureReady(player1, new EldraziAggressor());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new Ornithopter());

        assertThat(gqs.hasKeyword(gd, aggressor, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Loses haste when the other colorless creature leaves")
    void losesHasteWhenOtherColorlessCreatureLeaves() {
        Permanent aggressor = addCreatureReady(player1, new EldraziAggressor());
        Permanent ornithopter = addCreatureReady(player1, new Ornithopter());

        assertThat(gqs.hasKeyword(gd, aggressor, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(ornithopter);

        assertThat(gqs.hasKeyword(gd, aggressor, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Two Eldrazi Aggressors grant each other haste despite their red mana costs")
    void anotherDevoidCreatureGrantsHaste() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new EldraziAggressor());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new EldraziAggressor());

        assertThat(gqs.hasKeyword(gd, first, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("A colorless noncreature does not grant haste")
    void colorlessNoncreatureDoesNotGrantHaste() {
        Permanent aggressor = harness.addToBattlefieldAndReturn(player1, new EldraziAggressor());
        harness.addToBattlefield(player1, new StoneforgeMasterwork());

        assertThat(gqs.hasKeyword(gd, aggressor, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("A newly cast Aggressor can attack only while another colorless creature is controlled")
    void conditionalHasteAllowsAttackingOnTheTurnItEnters() {
        harness.castFromHand(player1, new EldraziAggressor(), "{2}{R}");
        harness.passBothPriorities();
        Permanent aggressor = findPermanent(player1, "Eldrazi Aggressor");

        assertThat(als.canAttack(gd, aggressor, player1.getId())).isFalse();

        Permanent other = harness.addToBattlefieldAndReturn(player1, new EldraziAggressor());

        assertThat(als.canAttack(gd, aggressor, player1.getId())).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(other);

        assertThat(als.canAttack(gd, aggressor, player1.getId())).isFalse();
    }
}
