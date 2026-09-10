package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExpeditionHealer.class, GrizzlyBears.class})
class ExpeditionHealerTest extends BaseCardTest {

    @Test
    @DisplayName("Has lifelink only while you control another Cleric")
    void conditionalLifelink() {
        Permanent healer = addCreatureReady(player1, new ExpeditionHealer());

        assertThat(gqs.hasKeyword(gd, healer, Keyword.LIFELINK)).isFalse();

        addCreatureReady(player1, new GrizzlyBears());
        assertThat(gqs.hasKeyword(gd, healer, Keyword.LIFELINK)).isFalse();

        addCreatureReady(player2, new ExpeditionHealer());
        assertThat(gqs.hasKeyword(gd, healer, Keyword.LIFELINK)).isFalse();

        addCreatureReady(player1, new ExpeditionHealer());
        assertThat(gqs.hasKeyword(gd, healer, Keyword.LIFELINK)).isTrue();
    }
}
