package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(KorScythemaster.class)
class KorScythemasterTest extends BaseCardTest {

    @Test
    @DisplayName("Has first strike while attacking")
    void hasFirstStrikeWhileAttacking() {
        Permanent scythemaster = addCreatureReady(player1, new KorScythemaster());
        scythemaster.setAttacking(true);

        assertThat(gqs.hasKeyword(gd, scythemaster, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not have first strike while not attacking")
    void doesNotHaveFirstStrikeWhileNotAttacking() {
        Permanent scythemaster = addCreatureReady(player1, new KorScythemaster());

        assertThat(gqs.hasKeyword(gd, scythemaster, Keyword.FIRST_STRIKE)).isFalse();
    }
}
