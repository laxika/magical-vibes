package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JolraelsCentaur;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CavalryMaster.class, JolraelsCentaur.class, GrizzlyBears.class})
class CavalryMasterTest extends BaseCardTest {

    @Test
    @DisplayName("Grants flanking to other flanking creatures you control")
    void grantsFlankingToOtherFlankingCreatures() {
        addCreatureReady(player1, new CavalryMaster());
        Permanent centaur = addCreatureReady(player1, new JolraelsCentaur());

        assertThat(gqs.hasKeyword(gd, centaur, Keyword.FLANKING)).isTrue();
    }

    @Test
    @DisplayName("Does not grant flanking to a creature without flanking")
    void doesNotGrantFlankingToNonFlankingCreature() {
        addCreatureReady(player1, new CavalryMaster());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLANKING)).isFalse();
    }

    @Test
    @DisplayName("Does not grant flanking to an opponent's creature")
    void doesNotGrantFlankingToOpponentsCreature() {
        addCreatureReady(player1, new CavalryMaster());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLANKING)).isFalse();
    }
}
