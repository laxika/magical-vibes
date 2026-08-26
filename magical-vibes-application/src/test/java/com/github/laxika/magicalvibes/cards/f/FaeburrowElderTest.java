package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FaeburrowElder.class, GrizzlyBears.class, FugitiveWizard.class, HillGiant.class})
class FaeburrowElderTest extends BaseCardTest {

    @Test
    void getsPlusOneForEachDistinctColorAmongControlledPermanents() {
        Permanent elder = addReadyElder();

        assertThat(gqs.getEffectivePower(gd, elder)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elder)).isEqualTo(2);

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player2, new HillGiant());

        assertThat(gqs.getEffectivePower(gd, elder)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elder)).isEqualTo(3);
    }

    @Test
    void addsOneManaOfEachDistinctControlledColor() {
        addReadyElder();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player2, new HillGiant());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    private Permanent addReadyElder() {
        Permanent elder = new Permanent(new FaeburrowElder());
        elder.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(elder);
        return elder;
    }
}
