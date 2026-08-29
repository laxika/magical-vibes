package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FelhideMinotaur;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CyclopsOfEternalFuryTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control have haste")
    void grantsHasteToCreaturesYouControl() {
        Permanent cyclops = addCreatureReady(player1, new CyclopsOfEternalFury());
        Permanent creature = addCreatureReady(player1, new FelhideMinotaur());
        Permanent opposingCreature = addCreatureReady(player2, new FelhideMinotaur());

        assertThat(gqs.hasKeyword(gd, cyclops, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.HASTE)).isFalse();
    }
}
