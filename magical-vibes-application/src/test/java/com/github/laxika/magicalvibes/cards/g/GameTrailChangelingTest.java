package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FieldMarshal;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GameTrailChangelingTest extends BaseCardTest {

    @Test
    @DisplayName("Changeling gets boost from Field Marshal (Soldier lord) due to being every creature type")
    void changelingGetsSubtypeBoost() {
        harness.addToBattlefield(player1, new FieldMarshal());
        harness.addToBattlefield(player1, new GameTrailChangeling());

        GameData gd = harness.getGameData();
        Permanent changeling = findPermanent(player1, "Game-Trail Changeling");

        assertThat(gqs.getEffectivePower(gd, changeling)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, changeling)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, changeling, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, changeling, Keyword.TRAMPLE)).isTrue();
    }
}
