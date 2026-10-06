package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GhostlyPrison;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SecretArcadeDustyParlor.class, GrizzlyBears.class, Forest.class, GhostlyPrison.class})
class SecretArcadeDustyParlorTest extends BaseCardTest {

    @Test
    void secretArcadeGrantsEnchantmentToOwnNonlandPermanentsOnly() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent room = castRoom(0);
        Card cardInHand = new GrizzlyBears();
        harness.setHand(player1, List.of(cardInHand));

        assertThat(room.isRoomDoorUnlocked(0)).isTrue();
        assertThat(gqs.isEnchantment(gd, ownCreature)).isTrue();
        assertThat(gqs.isEnchantment(gd, ownLand)).isFalse();
        assertThat(gqs.isEnchantment(gd, opponentCreature)).isFalse();
        assertThat(gqs.cardHasType(cardInHand, CardType.ENCHANTMENT, gd, player1.getId())).isFalse();
    }

    @Test
    void dustyParlorPutsEnchantmentManaValueCountersOnUpToOneCreature() {
        Permanent room = castRoom(1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GhostlyPrison()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThat(room.isRoomDoorUnlocked(1)).isTrue();
        harness.castEnchantment(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void secretArcadeMakesPermanentSpellsEnchantments() {
        Permanent room = castRoom(0);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.unlockRoomDoor(player1, 0, 1);

        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(room.isRoomDoorUnlocked(1)).isTrue();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void dustyParlorCanTargetAnOpponentsCreature() {
        castRoom(1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GhostlyPrison()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void dustyParlorAllowsChoosingNoTargetWhenACreatureExists() {
        castRoom(1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GhostlyPrison()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0);
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Ghostly Prison");
    }

    @Test
    void dustyParlorTriggerSurvivesRemovalOfTheRoom() {
        Permanent room = castRoom(1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GhostlyPrison()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, room);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void lockedSecretArcadeDoesNotGrantEnchantmentType() {
        castRoom(1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.isEnchantment(gd, creature)).isFalse();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    void lockedDustyParlorDoesNotTrigger() {
        castRoom(0);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GhostlyPrison()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Ghostly Prison");
    }

    @Test
    void dustyParlorUsesTheManaValueOfTheCastDoor() {
        castRoom(1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SecretArcadeDustyParlor()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    void dustyParlorDoesNotTriggerForAnOpponentsEnchantment() {
        castRoom(1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GhostlyPrison()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player2, "Ghostly Prison");
    }

    private Permanent castRoom(int doorIndex) {
        harness.setHand(player1, List.of(new SecretArcadeDustyParlor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, doorIndex == 0 ? 4 : 2);
        harness.castModalSorcery(player1, 0, doorIndex, List.of());
        harness.passBothPriorities();
        return gqs.findPermanentById(gd,
                harness.getPermanentId(player1, "Secret Arcade // Dusty Parlor"));
    }
}
