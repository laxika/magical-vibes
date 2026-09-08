package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodfireInfusion.class, GrizzlyBears.class, GiantSpider.class, HillGiant.class})
class BloodfireInfusionTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices the enchanted creature and deals its power to each creature")
    void sacrificesEnchantedCreatureAndDealsItsPowerToEachCreature() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BloodfireInfusion());
        aura.setAttachedTo(host.getId());
        Permanent smallCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent largeCreature = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Bloodfire Infusion");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(largeCreature).isIn(gd.playerBattlefields.get(player2.getId()));
        assertThat(largeCreature.getMarkedDamage()).isEqualTo(3);
    }
}
