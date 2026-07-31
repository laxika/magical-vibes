package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JuniperOrderAdvocateTest extends BaseCardTest {

    @Test
    @DisplayName("Green creatures you control get +1/+1 while the Advocate is untapped")
    void greenCreaturesGetBoost() {
        harness.addToBattlefield(player1, new JuniperOrderAdvocate());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Nongreen creatures you control are unaffected")
    void nonGreenCreaturesUnaffected() {
        harness.addToBattlefield(player1, new JuniperOrderAdvocate());
        harness.addToBattlefield(player1, new SavannahLions());
        Permanent lions = findPermanent(player1, "Savannah Lions");

        assertThat(gqs.getEffectivePower(gd, lions)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lions)).isEqualTo(1);
    }

    @Test
    @DisplayName("Green creatures an opponent controls are unaffected")
    void opponentGreenCreaturesUnaffected() {
        harness.addToBattlefield(player1, new JuniperOrderAdvocate());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost is lost while the Advocate is tapped")
    void boostLostWhileTapped() {
        harness.addToBattlefield(player1, new JuniperOrderAdvocate());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent advocate = findPermanent(player1, "Juniper Order Advocate");
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        advocate.tap();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost is lost once the Advocate leaves the battlefield")
    void boostLostWhenAdvocateLeaves() {
        harness.addToBattlefield(player1, new JuniperOrderAdvocate());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent advocate = findPermanent(player1, "Juniper Order Advocate");
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(advocate);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }
}
