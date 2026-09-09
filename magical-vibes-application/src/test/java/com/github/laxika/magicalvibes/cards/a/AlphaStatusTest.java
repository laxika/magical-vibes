package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlphaStatus.class, GrizzlyBears.class, AvenCloudchaser.class})
class AlphaStatusTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+2 for each other creature sharing a type with it")
    void boostsEnchantedCreatureForEachOtherSharingCreature() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new AvenCloudchaser());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AlphaStatus());
        aura.setAttachedTo(host.getId());

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(6);
    }

    @Test
    @DisplayName("The bonus updates as sharing creatures enter and leave")
    void bonusUpdatesDynamically() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AlphaStatus());
        aura.setAttachedTo(host.getId());

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(2);

        Permanent other = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(4);

        gd.playerBattlefields.get(player2.getId()).remove(other);
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(2);
    }

    @Test
    @DisplayName("Unattached Alpha Status and unrelated creatures provide no bonus")
    void unattachedOrUnrelatedProvidesNoBonus() {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AlphaStatus());
        Permanent host = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AvenCloudchaser());

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(2);

        aura.setAttachedTo(host.getId());
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(2);
    }

    @Test
    @DisplayName("The bonus ends when Alpha Status leaves the battlefield")
    void bonusEndsWhenAuraLeaves() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AlphaStatus());
        aura.setAttachedTo(host.getId());

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(2);
    }
}
