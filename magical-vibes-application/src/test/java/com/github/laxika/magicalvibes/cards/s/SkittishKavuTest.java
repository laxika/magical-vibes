package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SkittishKavuTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 when no opponent controls a white or blue creature")
    void getsBoostWithoutMatchingOpponentCreature() {
        harness.addToBattlefield(player1, new SkittishKavu());
        harness.addToBattlefield(player2, new HillGiant());

        Permanent kavu = findPermanent(player1, "Skittish Kavu");

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(2);
    }

    @Test
    @DisplayName("Loses +1/+1 while an opponent controls a white creature")
    void losesBoostToOpponentWhiteCreature() {
        harness.addToBattlefield(player1, new SkittishKavu());
        harness.addToBattlefield(player2, new SerraAngel());

        Permanent kavu = findPermanent(player1, "Skittish Kavu");

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(1);
    }

    @Test
    @DisplayName("Loses +1/+1 while an opponent controls a blue creature")
    void losesBoostToOpponentBlueCreature() {
        harness.addToBattlefield(player1, new SkittishKavu());
        harness.addToBattlefield(player2, new AirElemental());

        Permanent kavu = findPermanent(player1, "Skittish Kavu");

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(1);
    }

    @Test
    @DisplayName("Regains +1/+1 when the matching opponent creature leaves")
    void regainsBoostWhenMatchingOpponentCreatureLeaves() {
        harness.addToBattlefield(player1, new SkittishKavu());
        harness.addToBattlefield(player2, new SerraAngel());

        Permanent kavu = findPermanent(player1, "Skittish Kavu");
        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(1);

        Permanent angel = findPermanent(player2, "Serra Angel");
        gd.playerBattlefields.get(player2.getId()).remove(angel);

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(2);
    }
}
