package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.e.ElvishSpiritGuide;
import com.github.laxika.magicalvibes.cards.s.StormCrow;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JuniperOrderAdvocate.class, ElvishSpiritGuide.class, StormCrow.class})
class JuniperOrderAdvocateTest extends BaseCardTest {

    @Test
    @DisplayName("Green creatures you control get +1/+1 while the Advocate is untapped")
    void greenCreaturesGetBoost() {
        harness.addToBattlefield(player1, new JuniperOrderAdvocate());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new ElvishSpiritGuide());

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(3);
    }

    @Test
    @DisplayName("Nongreen creatures you control are unaffected")
    void nonGreenCreaturesUnaffected() {
        harness.addToBattlefield(player1, new JuniperOrderAdvocate());
        Permanent crow = harness.addToBattlefieldAndReturn(player1, new StormCrow());

        assertThat(gqs.getEffectivePower(gd, crow)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, crow)).isEqualTo(2);
    }

    @Test
    @DisplayName("Green creatures an opponent controls are unaffected")
    void opponentGreenCreaturesUnaffected() {
        harness.addToBattlefield(player1, new JuniperOrderAdvocate());
        Permanent elf = harness.addToBattlefieldAndReturn(player2, new ElvishSpiritGuide());

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost is lost while the Advocate is tapped")
    void boostLostWhileTapped() {
        Permanent advocate = harness.addToBattlefieldAndReturn(player1, new JuniperOrderAdvocate());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new ElvishSpiritGuide());

        advocate.tap();

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(2);

        advocate.untap();

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost is lost once the Advocate leaves the battlefield")
    void boostLostWhenAdvocateLeaves() {
        Permanent advocate = harness.addToBattlefieldAndReturn(player1, new JuniperOrderAdvocate());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new ElvishSpiritGuide());

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(advocate);

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
    }
}
