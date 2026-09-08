package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EldraziAggressor.class, Ornithopter.class, GrizzlyBears.class})
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
}
