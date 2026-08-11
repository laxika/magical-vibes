package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UrborgShamblerTest extends BaseCardTest {

    @Test
    @DisplayName("Other black creatures get -1/-1 regardless of controller")
    void debuffsOtherBlackCreatures() {
        harness.addToBattlefield(player1, new UrborgShambler());
        harness.addToBattlefield(player1, new ScatheZombies());
        harness.addToBattlefield(player2, new ScatheZombies());

        Permanent ownZombies = findPermanent(player1, "Scathe Zombies");
        Permanent opponentZombies = findPermanent(player2, "Scathe Zombies");

        assertThat(gqs.getEffectivePower(gd, ownZombies)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownZombies)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentZombies)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentZombies)).isEqualTo(1);
    }

    @Test
    @DisplayName("Urborg Shambler does not debuff itself")
    void doesNotDebuffItself() {
        harness.addToBattlefield(player1, new UrborgShambler());

        Permanent shambler = findPermanent(player1, "Urborg Shambler");

        assertThat(gqs.getEffectivePower(gd, shambler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, shambler)).isEqualTo(3);
    }

    @Test
    @DisplayName("Nonblack creatures are unaffected")
    void nonblackCreaturesAreUnaffected() {
        harness.addToBattlefield(player1, new UrborgShambler());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
}
