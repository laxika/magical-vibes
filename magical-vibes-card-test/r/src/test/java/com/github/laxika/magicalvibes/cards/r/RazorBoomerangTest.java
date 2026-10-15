package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.Curiosity;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RazorBoomerang.class, GrizzlyBears.class, Curiosity.class})
class RazorBoomerangTest extends BaseCardTest {

    @Test
    @DisplayName("Razor Boomerang deals 1 damage and returns to its owner's hand")
    void dealsDamageAndReturnsToHand() {
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent boomerang = harness.addToBattlefieldAndReturn(player1, new RazorBoomerang());
        boomerang.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).contains(boomerang.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(boomerang);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("damage from Razor Boomerang"));
    }

    @Test
    @DisplayName("Razor Boomerang stays unattached when the damage target becomes illegal")
    void illegalTargetStopsReturnToHand() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent boomerang = harness.addToBattlefieldAndReturn(player1, new RazorBoomerang());
        boomerang.setAttachedTo(creature.getId());

        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(boomerang);
        assertThat(boomerang.getAttachedTo()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(boomerang.getCard());
    }

    @Test
    @DisplayName("Equip costs two mana and grants the damage ability")
    void equipAndActivate() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent boomerang = harness.addToBattlefieldAndReturn(player1, new RazorBoomerang());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(boomerang.getAttachedTo()).isEqualTo(creature.getId());
        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(creature.isTapped()).isTrue();
        assertThat(boomerang.getAttachedTo()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(boomerang);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(boomerang.getCard());

        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        harness.assertInHand(player1, "Razor Boomerang");
    }

    @Test
    @DisplayName("A tapped creature cannot activate the granted tap ability")
    void tappedCreatureCannotActivate() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent boomerang = harness.addToBattlefieldAndReturn(player1, new RazorBoomerang());
        boomerang.setAttachedTo(creature.getId());
        creature.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(boomerang.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A summoning-sick creature cannot activate the granted tap ability")
    void summoningSickCreatureCannotActivate() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent boomerang = harness.addToBattlefieldAndReturn(player1, new RazorBoomerang());
        boomerang.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(boomerang.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Removing the equipped creature does not stop damage or the Equipment returning")
    void creatureRemovedInResponse() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent boomerang = harness.addToBattlefieldAndReturn(player1, new RazorBoomerang());
        boomerang.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertInHand(player1, "Razor Boomerang");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(boomerang);
    }

    @Test
    @DisplayName("Removing Razor Boomerang does not stop damage or return it from the graveyard")
    void equipmentRemovedInResponse() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent boomerang = harness.addToBattlefieldAndReturn(player1, new RazorBoomerang());
        boomerang.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(boomerang);
        gd.playerGraveyards.get(player1.getId()).add(boomerang.getCard());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Razor Boomerang");
        harness.assertNotInHand(player1, "Razor Boomerang");
    }

    @Test
    @DisplayName("Razor Boomerang damage does not trigger Curiosity on the equipped creature")
    void equipmentDamageDoesNotTriggerCreatureDamageAbility() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent boomerang = harness.addToBattlefieldAndReturn(player1, new RazorBoomerang());
        Permanent curiosity = harness.addToBattlefieldAndReturn(player1, new Curiosity());
        boomerang.setAttachedTo(creature.getId());
        curiosity.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertInHand(player1, "Razor Boomerang");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}