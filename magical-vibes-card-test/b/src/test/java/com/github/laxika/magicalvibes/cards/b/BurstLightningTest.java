package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BurstLightning.class, HillGiant.class, Plains.class})
class BurstLightningTest extends BaseCardTest {

    @Test
    void deals2DamageToAnyTargetWithoutKicker() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.castInstant(player1, 0, giant.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(giant.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void deals4DamageToAnyTargetWhenKicked() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 5);

        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.castKickedInstant(player1, 0, giant.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(giant.getId()));
    }

    @Test
    void dealsDamageToPlayer() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 5);

        int lifeBefore = gd.getLife(player2.getId());
        harness.castKickedInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    void cannotTargetLand() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature, planeswalker, battle, or player");
    }

    @Test
    void canDeclineKickerEvenWithEnoughMana() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 5);

        int lifeBefore = gd.getLife(player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 2);
        harness.assertInGraveyard(player1, "Burst Lightning");
    }

    @Test
    void canTargetItsControllerWhenKicked() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        int lifeBefore = gd.getLife(player1.getId());
        harness.castKickedInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore - 4);
    }

    @Test
    void cannotKickWithoutPayingTheAdditionalCost() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castKickedInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        harness.assertInHand(player1, "Burst Lightning");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotResolveWhenItsOnlyTargetLeavesTheBattlefield() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new BurstLightning(), new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 6);
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.castInstant(player1, 0, giant.getId());
        harness.castKickedInstant(player1, 0, giant.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
