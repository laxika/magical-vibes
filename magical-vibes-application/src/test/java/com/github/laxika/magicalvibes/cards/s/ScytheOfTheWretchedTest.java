package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.cards.t.TrollsOfTelJilad;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScytheOfTheWretched.class, TrollsOfTelJilad.class, Terror.class, SpikeshotGoblin.class, StalkingStones.class})
class ScytheOfTheWretchedTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+2")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new TrollsOfTelJilad());
        Permanent scythe = harness.addToBattlefieldAndReturn(player1, new ScytheOfTheWretched());
        scythe.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(8);
    }

    @Test
    @DisplayName("Equip {4} attaches Scythe to a creature you control")
    void equipAbilityAttachesScythe() {
        Permanent scythe = harness.addToBattlefieldAndReturn(player1, new ScytheOfTheWretched());
        Permanent creature = addCreatureReady(player1, new TrollsOfTelJilad());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(scythe.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Returns a creature damaged by the equipped creature and attaches to it")
    void returnsDamagedCreatureAndAttachesToIt() {
        Permanent attacker = addCreatureReady(player1, new TrollsOfTelJilad());
        attacker.setAttacking(true);

        Permanent scythe = harness.addToBattlefieldAndReturn(player1, new ScytheOfTheWretched());
        scythe.setAttachedTo(attacker.getId());

        Permanent blocker = addCreatureReady(player2, new TrollsOfTelJilad());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(blocker.getCard().getId()));
        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Trolls of Tel-Jilad"))
                .filter(permanent -> !permanent.getId().equals(attacker.getId()))
                .findFirst()
                .orElse(null);
        assertThat(returned).isNotNull();
        assertThat(scythe.getAttachedTo()).isEqualTo(returned.getId());
    }

    @Test
    @DisplayName("Does not return a creature that was not damaged by the equipped creature")
    void doesNotReturnUndamagedCreature() {
        Permanent scythe = harness.addToBattlefieldAndReturn(player1, new ScytheOfTheWretched());

        Permanent creature = addCreatureReady(player2, new TrollsOfTelJilad());

        harness.setHand(player1, List.of(new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(creature.getCard().getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Trolls of Tel-Jilad"));
        assertThat(scythe.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Returns a creature damaged before Scythe was attached when it later dies")
    void returnsCreatureDamagedBeforeScytheWasAttached() {
        Permanent goblin = addCreatureReady(player1, new SpikeshotGoblin());
        Permanent scythe = harness.addToBattlefieldAndReturn(player1, new ScytheOfTheWretched());
        Permanent target = addCreatureReady(player2, new TrollsOfTelJilad());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 1, null, goblin.getId());
        harness.passBothPriorities();
        assertThat(scythe.getAttachedTo()).isEqualTo(goblin.getId());

        harness.setHand(player1, List.of(new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(target.getCard().getId()));
        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Trolls of Tel-Jilad"))
                .findFirst()
                .orElse(null);
        assertThat(returned).isNotNull();
        assertThat(scythe.getAttachedTo()).isEqualTo(returned.getId());
    }

    @Test
    @DisplayName("Returns a damaged animated land without attaching to the returned land")
    void returnsAnimatedLandWithoutAttaching() {
        Permanent goblin = addCreatureReady(player1, new SpikeshotGoblin());
        Permanent scythe = harness.addToBattlefieldAndReturn(player1, new ScytheOfTheWretched());
        scythe.setAttachedTo(goblin.getId());
        Permanent stones = harness.addToBattlefieldAndReturn(player2, new StalkingStones());

        harness.addMana(player2, ManaColor.COLORLESS, 6);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, stones)).isTrue();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 0, null, stones.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotInGraveyard(player2, "Stalking Stones");
        Permanent returned = findPermanent(player1, "Stalking Stones");
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(scythe.getAttachedTo()).isEqualTo(goblin.getId());
    }

    @Test
    @DisplayName("Equipment controller receives the creature even when another player controls the equipped creature")
    void returnsCreatureToEquipmentController() {
        Permanent scythe = harness.addToBattlefieldAndReturn(player1, new ScytheOfTheWretched());
        Permanent goblin = addCreatureReady(player2, new SpikeshotGoblin());
        Permanent target = addCreatureReady(player2, new TrollsOfTelJilad());
        scythe.setAttachedTo(goblin.getId());

        harness.addMana(player2, ManaColor.RED, 1);
        harness.activateAbility(player2, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertNotInGraveyard(player2, "Trolls of Tel-Jilad");
        Permanent returned = findPermanent(player1, "Trolls of Tel-Jilad");
        assertThat(returned.getCard().getId()).isEqualTo(target.getCard().getId());
        assertThat(scythe.getAttachedTo()).isEqualTo(returned.getId());
    }
}
