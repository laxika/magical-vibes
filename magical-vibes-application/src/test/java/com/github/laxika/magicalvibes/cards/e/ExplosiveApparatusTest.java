package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VivienReid;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExplosiveApparatus.class, GrizzlyBears.class, VivienReid.class})
class ExplosiveApparatusTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and deals 2 damage to target creature")
    void sacrificesItselfAndDealsDamageToCreature() {
        harness.addToBattlefield(player1, new ExplosiveApparatus());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertInGraveyard(player1, "Explosive Apparatus");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrifices itself and deals 2 damage to target player")
    void sacrificesItselfAndDealsDamageToPlayer() {
        harness.addToBattlefield(player1, new ExplosiveApparatus());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertInGraveyard(player1, "Explosive Apparatus");
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @CardUsed({ExplosiveApparatus.class, VivienReid.class})
    @DisplayName("Deals damage to a planeswalker after sacrificing the source")
    void dealsDamageToPlaneswalker() {
        harness.addToBattlefield(player1, new ExplosiveApparatus());
        Permanent vivien = harness.addToBattlefieldAndReturn(player2, new VivienReid());
        vivien.setCounterCount(CounterType.LOYALTY, 5);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, vivien.getId());

        harness.assertInGraveyard(player1, "Explosive Apparatus");
        assertThat(vivien.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        harness.passBothPriorities();
        assertThat(vivien.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertLife(player2, 20);
    }

    @Test
    @CardUsed({ExplosiveApparatus.class})
    @DisplayName("Can target its controller and damage waits for resolution")
    void canDamageItsController() {
        harness.addToBattlefield(player1, new ExplosiveApparatus());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player1.getId());

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Explosive Apparatus");
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
    }

    @Test
    @CardUsed({ExplosiveApparatus.class})
    @DisplayName("A tapped Apparatus cannot activate its ability")
    void cannotActivateWhileTapped() {
        Permanent apparatus = harness.addToBattlefieldAndReturn(player1, new ExplosiveApparatus());
        apparatus.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Explosive Apparatus");
        harness.assertNotInGraveyard(player1, "Explosive Apparatus");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @CardUsed({ExplosiveApparatus.class})
    @DisplayName("Activation requires three mana and does not sacrifice on failed payment")
    void cannotActivateWithoutEnoughMana() {
        Permanent apparatus = harness.addToBattlefieldAndReturn(player1, new ExplosiveApparatus());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Explosive Apparatus");
        harness.assertNotInGraveyard(player1, "Explosive Apparatus");
        assertThat(apparatus.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({ExplosiveApparatus.class})
    @DisplayName("An ordinary artifact is not a legal damage target")
    void cannotTargetAnOrdinaryArtifact() {
        harness.addToBattlefield(player1, new ExplosiveApparatus());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ExplosiveApparatus());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Explosive Apparatus");
        harness.assertNotInGraveyard(player1, "Explosive Apparatus");
        assertThat(gd.stack).isEmpty();
    }
}
