package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BrothersOfFire;
import com.github.laxika.magicalvibes.cards.i.Inferno;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarkSphere.class, Inferno.class, BrothersOfFire.class})
class DarkSphereTest extends BaseCardTest {

    @Test
    void preventsHalfOfTheNextDamageFromChosenSource() {
        harness.setLife(player2, 20);
        Permanent sphere = harness.addToBattlefieldAndReturn(player2, new DarkSphere());
        Inferno inferno = new Inferno();
        harness.castFromHand(player1, inferno, "{5}{R}{R}");

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(sphere), null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, inferno.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        harness.assertInGraveyard(player2, "Dark Sphere");
    }

    @Test
    void doesNotPreventDamageFromAnotherSource() {
        harness.setLife(player2, 20);
        Permanent sphere = harness.addToBattlefieldAndReturn(player2, new DarkSphere());
        Permanent brothers = harness.addToBattlefieldAndReturn(player1, new BrothersOfFire());
        brothers.setSummoningSick(false);
        Inferno inferno = new Inferno();
        harness.castFromHand(player1, inferno, "{5}{R}{R}");

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(sphere), null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, inferno.getId());

        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(brothers), null,
                player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);

        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    void roundsHalfDownForOneDamage() {
        harness.setLife(player2, 20);
        Permanent sphere = harness.addToBattlefieldAndReturn(player2, new DarkSphere());
        Permanent brothers = harness.addToBattlefieldAndReturn(player1, new BrothersOfFire());
        brothers.setSummoningSick(false);

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(sphere), null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, brothers.getId());

        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(brothers), null,
                player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertInGraveyard(player2, "Dark Sphere");
    }
}
