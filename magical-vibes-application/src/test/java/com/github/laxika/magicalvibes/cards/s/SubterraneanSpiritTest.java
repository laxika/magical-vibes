package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SubterraneanSpiritTest extends BaseCardTest {

    private Permanent addReadySpirit() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new SubterraneanSpirit());
        spirit.setSummoningSick(false);
        return spirit;
    }

    @Test
    @DisplayName("Deals 1 damage to each creature without flying, sparing flyers")
    void damagesOnlyNonFlyers() {
        addReadySpirit();
        harness.addToBattlefield(player2, new FugitiveWizard());  // 1/1 non-flying -> dies
        harness.addToBattlefield(player2, new SuntailHawk());     // 1/1 flying -> survives
        harness.addToBattlefield(player2, new GrizzlyBears());    // 2/2 non-flying -> survives

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        harness.assertOnBattlefield(player2, "Suntail Hawk");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Protection from red prevents the damage from its own red ability")
    void protectionFromRedPreventsSelfDamage() {
        Permanent spirit = addReadySpirit();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Subterranean Spirit");
        assertThat(spirit.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not damage players")
    void doesNotDamagePlayers() {
        addReadySpirit();
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
