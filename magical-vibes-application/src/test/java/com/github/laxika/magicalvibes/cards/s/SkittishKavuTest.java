package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ArdentSoldier;
import com.github.laxika.magicalvibes.cards.l.LlanowarCavalry;
import com.github.laxika.magicalvibes.cards.v.VodalianMerchant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkittishKavu.class, ArdentSoldier.class, LlanowarCavalry.class,
        SterlingGrove.class, VodalianMerchant.class})
class SkittishKavuTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 when no opponent controls a white or blue creature")
    void getsBoostWithoutMatchingOpponentCreature() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player1, new SkittishKavu());
        harness.addToBattlefield(player2, new LlanowarCavalry());

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(2);
    }

    @Test
    @DisplayName("Loses +1/+1 while an opponent controls a white creature")
    void losesBoostToOpponentWhiteCreature() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player1, new SkittishKavu());
        harness.addToBattlefield(player2, new ArdentSoldier());

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(1);
    }

    @Test
    @DisplayName("Loses +1/+1 while an opponent controls a blue creature")
    void losesBoostToOpponentBlueCreature() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player1, new SkittishKavu());
        harness.addToBattlefield(player2, new VodalianMerchant());

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ignores a white noncreature permanent controlled by an opponent")
    void ignoresOpponentWhiteNoncreaturePermanent() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player1, new SkittishKavu());
        harness.addToBattlefield(player2, new SterlingGrove());

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ignores a white creature controlled by its controller")
    void ignoresOwnMatchingCreature() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player1, new SkittishKavu());
        harness.addToBattlefield(player1, new ArdentSoldier());

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(2);
    }

    @Test
    @DisplayName("Regains +1/+1 when the matching opponent creature leaves")
    void regainsBoostWhenMatchingOpponentCreatureLeaves() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player1, new SkittishKavu());
        Permanent whiteCreature = harness.addToBattlefieldAndReturn(player2, new ArdentSoldier());
        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(1);

        gd.playerBattlefields.get(player2.getId()).remove(whiteCreature);

        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(2);
    }
}
