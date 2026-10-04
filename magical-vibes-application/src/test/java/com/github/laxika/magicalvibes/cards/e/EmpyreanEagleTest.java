package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.z.ZephyrCharge;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmpyreanEagle.class, GrizzlyBears.class, SuntailHawk.class, Unsummon.class, ZephyrCharge.class})
class EmpyreanEagleTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts other flying creatures you control")
    void boostsOwnFlyers() {
        harness.addToBattlefield(player1, new EmpyreanEagle());
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());

        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hawk)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost itself or nonflying creatures")
    void excludesSourceAndNonflyers() {
        Permanent eagle = harness.addToBattlefieldAndReturn(player1, new EmpyreanEagle());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, eagle)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, eagle)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost flying creatures controlled by an opponent")
    void excludesOpponentsFlyers() {
        harness.addToBattlefield(player1, new EmpyreanEagle());
        Permanent hawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());

        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hawk)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple Eagles boost each other and their bonuses stack")
    void multipleEaglesStack() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new EmpyreanEagle());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new EmpyreanEagle());
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hawk)).isEqualTo(3);
    }

    @Test
    @DisplayName("The bonus ends when the Eagle leaves the battlefield")
    void bonusEndsWhenSourceLeaves() {
        Permanent eagle = harness.addToBattlefieldAndReturn(player1, new EmpyreanEagle());
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hawk)).isEqualTo(2);

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, eagle.getId());

        harness.assertNotOnBattlefield(player1, "Empyrean Eagle");
        harness.assertInHand(player1, "Empyrean Eagle");
        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hawk)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature that gains flying starts receiving the bonus")
    void boostsCreatureThatGainsFlying() {
        harness.addToBattlefield(player1, new ZephyrCharge());
        harness.addToBattlefield(player1, new EmpyreanEagle());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }
}
