package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FieldMarshal;
import com.github.laxika.magicalvibes.cards.e.ElvishEulogist;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WoodlandChangeling.class, FieldMarshal.class, ElvishEulogist.class})
class WoodlandChangelingTest extends BaseCardTest {

    @Test
    @DisplayName("Changeling gets boost from Field Marshal (Soldier lord) due to being every creature type")
    void changelingGetsSubtypeBoost() {
        harness.addToBattlefield(player1, new FieldMarshal());
        Permanent changeling = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());

        assertThat(gqs.getEffectivePower(gd, changeling)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, changeling)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, changeling, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Woodland Changeling counts as an Elf card in the graveyard")
    void changelingCountsAsElfInGraveyard() {
        harness.addToBattlefield(player1, new ElvishEulogist());
        harness.setGraveyard(player1, List.of(new WoodlandChangeling()));
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertInGraveyard(player1, "Woodland Changeling");
    }
}
