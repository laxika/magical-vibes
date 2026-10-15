package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrazingWhiptail;
import com.github.laxika.magicalvibes.cards.j.JaceCunningCastaway;
import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BurningSunsAvatar.class, RaptorCompanion.class, GrazingWhiptail.class, JaceCunningCastaway.class})
class BurningSunsAvatarTest extends BaseCardTest {

    @Test
    @DisplayName("ETB cannot target its controller with opponent damage")
    void etbCannotDamageController() {
        harness.setHand(player1, List.of(new BurningSunsAvatar()));
        harness.addMana(player1, ManaColor.RED, 6);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(player1.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent or planeswalker");
    }

    @Test
    @DisplayName("ETB deals 3 damage to opponent and 3 damage to target creature")
    void etbDeals3DamageToOpponentAnd3DamageToCreature() {
        harness.addToBattlefield(player2, new RaptorCompanion());
        harness.setHand(player1, List.of(new BurningSunsAvatar()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.setLife(player2, 20);

        UUID creatureId = harness.getPermanentId(player2, "Raptor Companion");
        harness.castCreature(player1, 0, List.of(player2.getId(), creatureId));

        // Resolve the creature spell and put its triggered ability on the stack.
        harness.passBothPriorities();
        // Resolve ETB triggered ability
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        // Opponent takes 3 damage
        harness.assertLife(player2, 17);
        // Raptor Companion (3/1) takes 3 damage and dies
        harness.assertNotOnBattlefield(player2, "Raptor Companion");
        harness.assertInGraveyard(player2, "Raptor Companion");
    }

    @Test
    @DisplayName("ETB deals 3 damage to opponent when no creature is targeted")
    void etbDeals3DamageToOpponentOnly() {
        harness.setHand(player1, List.of(new BurningSunsAvatar()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0, List.of(player2.getId()));

        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB triggered ability
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("ETB deals 3 damage to a creature with 4 toughness but does not kill it")
    void etbDeals3DamageDoesNotKillBigCreature() {
        Permanent bigCreature = harness.addToBattlefieldAndReturn(player2, new GrazingWhiptail());
        harness.setHand(player1, List.of(new BurningSunsAvatar()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.setLife(player2, 20);

        UUID creatureId = bigCreature.getId();
        harness.castCreature(player1, 0, List.of(player2.getId(), creatureId));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 17);
        harness.assertOnBattlefield(player2, "Grazing Whiptail");
        assertThat(bigCreature.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Resolving creature puts ETB triggered ability on the stack")
    void resolvingCreaturePutsEtbOnStack() {
        harness.setHand(player1, List.of(new BurningSunsAvatar()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0, List.of(player2.getId()));

        // Resolve the creature spell and put its triggered ability on the stack.
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Burning Sun's Avatar");
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Burning Sun's Avatar");
    }

    @Test
    @DisplayName("Creature enters battlefield when cast without targets")
    void entersWithoutTargets() {
        harness.setHand(player1, List.of(new BurningSunsAvatar()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);

        // Resolve creature spell
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Burning Sun's Avatar");
    }

    @Test
    void canDamageOwnCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrazingWhiptail());
        harness.setHand(player1, List.of(new BurningSunsAvatar()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castCreature(player1, 0, List.of(player2.getId(), creature.getId()));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertOnBattlefield(player1, "Grazing Whiptail");
        assertThat(creature.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetOwnPlaneswalkerWithoutCreatureTarget() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new JaceCunningCastaway());
        harness.setHand(player1, List.of(new BurningSunsAvatar()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castCreature(player1, 0, List.of(planeswalker.getId()));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Jace, Cunning Castaway");
        harness.assertInGraveyard(player1, "Jace, Cunning Castaway");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void damagesPlaneswalkerAndCreature() {
        Permanent planeswalker = harness.enterBattlefieldAndReturn(player2, new JaceCunningCastaway());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrazingWhiptail());
        harness.setHand(player1, List.of(new BurningSunsAvatar()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castCreature(player1, 0, List.of(planeswalker.getId(), creature.getId()));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Jace, Cunning Castaway");
        harness.assertNotOnBattlefield(player2, "Jace, Cunning Castaway");
        harness.assertOnBattlefield(player2, "Grazing Whiptail");
        assertThat(creature.getMarkedDamage()).isEqualTo(3);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void stillDamagesOpponentWhenCreatureTargetLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrazingWhiptail());
        harness.setHand(player1, List.of(new BurningSunsAvatar()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castCreature(player1, 0, List.of(player2.getId(), creature.getId()));
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, creature));
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertInHand(player2, "Grazing Whiptail");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void stillDamagesCreatureWhenPlaneswalkerTargetLeaves() {
        Permanent planeswalker = harness.enterBattlefieldAndReturn(player2, new JaceCunningCastaway());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrazingWhiptail());
        harness.setHand(player1, List.of(new BurningSunsAvatar()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castCreature(player1, 0, List.of(planeswalker.getId(), creature.getId()));
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, planeswalker));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Jace, Cunning Castaway");
        harness.assertOnBattlefield(player2, "Grazing Whiptail");
        assertThat(creature.getMarkedDamage()).isEqualTo(3);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggerResolvesAfterAvatarLeavesBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrazingWhiptail());
        harness.setHand(player1, List.of(new BurningSunsAvatar()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castCreature(player1, 0, List.of(player2.getId(), creature.getId()));
        harness.passBothPriorities();
        Permanent avatar = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof BurningSunsAvatar)
                .findFirst().orElseThrow();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, avatar));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Burning Sun's Avatar");
        harness.assertLife(player2, 17);
        assertThat(creature.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotResolveWhenAllTargetsLeave() {
        Permanent planeswalker = harness.enterBattlefieldAndReturn(player2, new JaceCunningCastaway());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrazingWhiptail());
        harness.setHand(player1, List.of(new BurningSunsAvatar()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castCreature(player1, 0, List.of(planeswalker.getId(), creature.getId()));
        harness.passBothPriorities();

        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToHand(gd, planeswalker);
            harness.getPermanentRemovalService().removePermanentToHand(gd, creature);
        });
        harness.passBothPriorities();

        harness.assertInHand(player2, "Jace, Cunning Castaway");
        harness.assertInHand(player2, "Grazing Whiptail");
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Burning Sun's Avatar");
        assertThat(gd.stack).isEmpty();
    }
}
