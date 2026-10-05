package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AqueousForm;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Omenspeaker;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MistcutterHydra.class, Cancel.class, GrizzlyBears.class, Unsummon.class, Omenspeaker.class, AqueousForm.class})
class MistcutterHydraTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with X +1/+1 counters")
    void entersWithXCounters() {
        harness.setHand(player1, List.of(new MistcutterHydra()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent hydra = findPermanent(player1, "Mistcutter Hydra");
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot be countered by Cancel")
    void cannotBeCountered() {
        MistcutterHydra hydra = new MistcutterHydra();
        harness.setHand(player1, List.of(hydra));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, hydra.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mistcutter Hydra");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Has protection from blue")
    void hasProtectionFromBlue() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player1, new MistcutterHydra());

        assertThat(gqs.hasProtectionFrom(gd, hydra, CardColor.BLUE)).isTrue();
    }

    @Test
    @DisplayName("Cannot be targeted by a blue spell")
    void cannotBeTargetedByBlueSpell() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player1, new MistcutterHydra());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, hydra.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid target");
    }

    @Test
    @DisplayName("Casting with X equal to zero puts Hydra into the graveyard")
    void diesWhenCastWithZeroX() {
        harness.setHand(player1, List.of(new MistcutterHydra()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mistcutter Hydra");
        harness.assertInGraveyard(player1, "Mistcutter Hydra");
    }

    @Test
    @DisplayName("Haste allows Hydra to attack the turn it is cast")
    void attacksTheTurnItIsCast() {
        harness.setHand(player1, List.of(new MistcutterHydra()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(findPermanent(player1, "Mistcutter Hydra").isAttacking()).isTrue();
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Blue creatures cannot block Hydra")
    void blueCreatureCannotBlock() {
        harness.setHand(player1, List.of(new MistcutterHydra()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();
        addCreatureReady(player2, new Omenspeaker());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Hydra can block a blue creature and prevents its combat damage")
    void preventsBlueCombatDamageWhileBlocking() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player2, new MistcutterHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player1, new Omenspeaker());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player2, "Mistcutter Hydra");
        assertThat(hydra.getMarkedDamage()).isZero();
        assertThat(findPermanent(player1, "Omenspeaker").getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Even its controller cannot enchant Hydra with a blue Aura")
    void cannotBeTargetedByOwnBlueAura() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player1, new MistcutterHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new AqueousForm()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, hydra.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Aqueous Form");
    }

    @Test
    @DisplayName("Protection from blue does not prevent blocking or damage from green creatures")
    void greenCreatureCanBlockAndDealDamage() {
        harness.setHand(player1, List.of(new MistcutterHydra()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Mistcutter Hydra");
        assertThat(findPermanent(player1, "Mistcutter Hydra").getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
    }
}
