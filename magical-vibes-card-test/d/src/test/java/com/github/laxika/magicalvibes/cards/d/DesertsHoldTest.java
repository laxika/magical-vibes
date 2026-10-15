package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.f.FeralProwler;
import com.github.laxika.magicalvibes.cards.h.HashepOasis;
import com.github.laxika.magicalvibes.cards.o.OasisRitualist;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DesertsHold.class, FeralProwler.class, HashepOasis.class, BottleGnomes.class, OasisRitualist.class})
class DesertsHoldTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gains 3 life when you control a Desert")
    void etbGainsLifeWithDesertOnBattlefield() {
        harness.addToBattlefield(player1, new HashepOasis());

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new FeralProwler());

        harness.setHand(player1, List.of(new DesertsHold()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, bears.getId());

        harness.passBothPriorities(); // resolve the Aura — ETB trigger onto stack
        harness.passBothPriorities(); // resolve the ETB trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("ETB gains 3 life when a Desert card is in your graveyard")
    void etbGainsLifeWithDesertInGraveyard() {
        harness.setGraveyard(player1, List.of(new HashepOasis()));

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new FeralProwler());

        harness.setHand(player1, List.of(new DesertsHold()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, bears.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("ETB does not gain life without any Desert")
    void etbNoLifeWithoutDesert() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new FeralProwler());

        harness.setHand(player1, List.of(new DesertsHold()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, bears.getId());

        harness.passBothPriorities(); // resolve the Aura — intervening-if fails, no trigger queued

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        // The Aura still attached despite the life-gain condition not being met.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Desert's Hold") && p.isAttached());
    }

    @Test
    @DisplayName("Enchanted creature cannot attack")
    void enchantedCreatureCannotAttack() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new FeralProwler());
        bears.setSummoningSick(false);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DesertsHold());
        aura.setAttachedTo(bears.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Enchanted creature cannot block")
    void enchantedCreatureCannotBlock() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new FeralProwler());
        blocker.setSummoningSick(false);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DesertsHold());
        aura.setAttachedTo(blocker.getId());

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new FeralProwler());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Enchanted creature cannot activate its abilities")
    void enchantedCreatureCannotActivateAbilities() {
        Permanent gnomes = harness.addToBattlefieldAndReturn(player1, new BottleGnomes());
        gnomes.setSummoningSick(false);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DesertsHold());
        aura.setAttachedTo(gnomes.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        // A legal creature target must exist so the Aura is castable at all; targeting the
        // noncreature Desert then fails with the specific target-filter error.
        harness.addToBattlefield(player2, new FeralProwler());

        Permanent desert = harness.addToBattlefieldAndReturn(player1, new HashepOasis());

        harness.setHand(player1, List.of(new DesertsHold()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, desert.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void opponentDesertsDoNotEnableLifeGain() {
        harness.addToBattlefield(player2, new HashepOasis());
        harness.setGraveyard(player2, List.of(new HashepOasis()));
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FeralProwler());
        harness.setHand(player1, List.of(new DesertsHold()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void losingOnlyDesertBeforeResolutionPreventsLifeGain() {
        harness.setGraveyard(player1, List.of(new HashepOasis()));
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FeralProwler());
        harness.setHand(player1, List.of(new DesertsHold()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    void desertInBothZonesStillGainsOnlyThreeLife() {
        harness.addToBattlefield(player1, new HashepOasis());
        harness.setGraveyard(player1, List.of(new HashepOasis()));
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FeralProwler());
        harness.setHand(player1, List.of(new DesertsHold()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    @Test
    void enchantedCreatureCannotActivateManaAbilities() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new OasisRitualist());
        creature.setSummoningSick(false);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DesertsHold());
        aura.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
        assertThat(creature.isTapped()).isFalse();
    }
}
