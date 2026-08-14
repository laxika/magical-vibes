package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BrazenScourgeTest extends BaseCardTest {

    @Test
    @DisplayName("Brazen Scourge has haste on the battlefield")
    void hasHasteOnBattlefield() {
        harness.addToBattlefield(player1, new BrazenScourge());

        Permanent scourge = findPermanent(player1, "Brazen Scourge");

        assertThat(scourge.hasKeyword(Keyword.HASTE)).isTrue();
    }
}
