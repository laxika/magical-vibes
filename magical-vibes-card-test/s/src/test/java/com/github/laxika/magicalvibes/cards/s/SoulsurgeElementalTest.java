package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.OgreSentry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoulsurgeElemental.class, OgreSentry.class, Forest.class})
class SoulsurgeElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of creatures its controller controls")
    void powerEqualsControlledCreatures() {
        Permanent elemental = addCreatureReady(player1, new SoulsurgeElemental());
        harness.addToBattlefield(player1, new OgreSentry());
        harness.addToBattlefield(player1, new OgreSentry());
        harness.addToBattlefield(player2, new OgreSentry());

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(1);
    }

    @Test
    @DisplayName("Power updates as creatures enter and leave its controller's battlefield")
    void powerUpdatesDynamically() {
        Permanent elemental = addCreatureReady(player1, new SoulsurgeElemental());

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(1);

        harness.addToBattlefield(player1, new OgreSentry());
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Ogre Sentry"));
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(1);
    }

    @Test
    void noncreaturePermanentsDoNotIncreasePower() {
        Permanent elemental = addCreatureReady(player1, new SoulsurgeElemental());
        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(1);
    }

    @Test
    void characteristicPowerWorksInGraveyardWithoutCountingItself() {
        SoulsurgeElemental elemental = new SoulsurgeElemental();
        harness.setGraveyard(player1, List.of(elemental));
        harness.addToBattlefield(player2, new OgreSentry());

        assertThat(gqs.getEffectiveCardPower(gd, elemental)).isZero();
        harness.addToBattlefield(player1, new OgreSentry());
        assertThat(gqs.getEffectiveCardPower(gd, elemental)).isEqualTo(1);
    }

    @Test
    void firstStrikeKillsBlockerBeforeItCanDealDamage() {
        Permanent elemental = addCreatureReady(player1, new SoulsurgeElemental());
        harness.addToBattlefield(player1, new OgreSentry());
        harness.addToBattlefield(player1, new OgreSentry());
        elemental.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new OgreSentry());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertInGraveyard(player2, "Ogre Sentry");
        harness.assertOnBattlefield(player1, "Soulsurge Elemental");
        assertThat(elemental.getMarkedDamage()).isZero();
    }
}
