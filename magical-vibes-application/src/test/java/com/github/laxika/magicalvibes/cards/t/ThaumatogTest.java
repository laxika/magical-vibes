package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThaumatogTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a land gives Thaumatog +1/+1 until end of turn")
    void sacrificingLandBoostsSelf() {
        Permanent thaumatog = addReadyThaumatog();
        harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(thaumatog.getPowerModifier()).isEqualTo(1);
        assertThat(thaumatog.getToughnessModifier()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Sacrificing an enchantment gives Thaumatog +1/+1 until end of turn")
    void sacrificingEnchantmentBoostsSelf() {
        Permanent thaumatog = addReadyThaumatog();
        harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(thaumatog.getPowerModifier()).isEqualTo(1);
        assertThat(thaumatog.getToughnessModifier()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Glorious Anthem");
    }

    @Test
    @DisplayName("Each ability requires its matching permanent type")
    void requiresMatchingPermanentType() {
        addReadyThaumatog();
        harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyThaumatog() {
        Permanent permanent = new Permanent(new Thaumatog());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }
}
