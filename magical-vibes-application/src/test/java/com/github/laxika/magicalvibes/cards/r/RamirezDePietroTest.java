package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RamirezDePietro.class)
class RamirezDePietroTest extends BaseCardTest {

    @Test
    @DisplayName("Ramirez DePietro has first strike")
    void hasFirstStrike() {
        Permanent ramirez = harness.addToBattlefieldAndReturn(player1, new RamirezDePietro());

        assertThat(gqs.hasKeyword(gd, ramirez, Keyword.FIRST_STRIKE)).isTrue();
    }
}
