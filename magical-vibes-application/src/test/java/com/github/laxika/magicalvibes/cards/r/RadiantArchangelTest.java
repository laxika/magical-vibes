package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CloudElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RadiantArchangelTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each other creature with flying on the battlefield")
    void boostsForOtherFlyingCreaturesOnBattlefield() {
        Permanent radiant = harness.addToBattlefieldAndReturn(player1, new RadiantArchangel());
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.addToBattlefield(player2, new CloudElemental());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, radiant)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, radiant)).isEqualTo(5);
    }

    @Test
    @DisplayName("Updates as other flying creatures enter and leave")
    void updatesDynamically() {
        Permanent radiant = harness.addToBattlefieldAndReturn(player1, new RadiantArchangel());

        assertThat(gqs.getEffectivePower(gd, radiant)).isEqualTo(3);

        Permanent hawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        assertThat(gqs.getEffectivePower(gd, radiant)).isEqualTo(4);

        gd.playerBattlefields.get(player2.getId()).remove(hawk);
        assertThat(gqs.getEffectivePower(gd, radiant)).isEqualTo(3);
    }
}
