package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FieldMarshal;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WoodlandChangelingTest extends BaseCardTest {

    @Test
    @DisplayName("Changeling gets boost from Field Marshal (Soldier lord) due to being every creature type")
    void changelingGetsSubtypeBoost() {
        harness.addToBattlefield(player1, new FieldMarshal());
        harness.addToBattlefield(player1, new WoodlandChangeling());

        GameData gd = harness.getGameData();
        Permanent changeling = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Woodland Changeling"))
                .findFirst()
                .orElseThrow();

        assertThat(gqs.getEffectivePower(gd, changeling)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, changeling)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, changeling, Keyword.FIRST_STRIKE)).isTrue();
    }
}
