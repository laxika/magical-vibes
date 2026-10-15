package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SarkhansRage.class, ColossalDreadmaw.class, ShivanDragon.class})
class SarkhansRageTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage to the target player and 2 damage to you without a Dragon")
    void dealsDamageAndHurtsControllerWithoutDragon() {
        castAt(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not hurt you when you control a Dragon")
    void doesNotHurtControllerWithDragon() {
        harness.addToBattlefield(player1, new ShivanDragon());
        Permanent target = addCreatureReady(player2, new ColossalDreadmaw());

        castAt(target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(5);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void targetingYourselfDealsBothDamageAmountsWithoutDragon() {
        castAt(player1.getId());
        harness.assertLife(player1, 13);
        harness.assertLife(player2, 20);
    }

    @Test
    void opponentsDragonDoesNotPreventControllerDamage() {
        harness.addToBattlefield(player2, new ShivanDragon());
        castAt(player2.getId());
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 15);
    }

    @Test
    void dragonEnteringBeforeResolutionPreventsControllerDamage() {
        prepareCast();
        harness.castInstant(player1, 0, player2.getId());
        harness.addToBattlefield(player1, new ShivanDragon());
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 15);
    }

    @Test
    void dragonLeavingBeforeResolutionAllowsControllerDamage() {
        harness.addToBattlefield(player1, new ShivanDragon());
        prepareCast();
        harness.castInstant(player1, 0, player2.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 15);
    }

    @Test
    void illegalSoleTargetPreventsControllerDamageToo() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        prepareCast();
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Sarkhan's Rage");
    }

    @Test
    void lethalDamageToYourOnlyDragonDoesNotCauseControllerDamage() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new ShivanDragon());
        castAt(dragon.getId());
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Shivan Dragon");
        harness.assertNotOnBattlefield(player1, "Shivan Dragon");
    }

    private void castAt(java.util.UUID targetId) {
        prepareCast();
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new SarkhansRage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
