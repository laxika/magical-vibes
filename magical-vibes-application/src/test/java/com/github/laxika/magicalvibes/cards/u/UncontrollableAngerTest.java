package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.r.ReitoLantern;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UncontrollableAnger.class, IsamaruHoundOfKonda.class, ReitoLantern.class})
class UncontrollableAngerTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast Uncontrollable Anger during opponent's turn thanks to flash")
    void canCastDuringOpponentsTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());
        harness.setHand(player1, List.of(new UncontrollableAnger()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gs.passPriority(gd, player2);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("Resolving Uncontrollable Anger attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());
        harness.setHand(player1, List.of(new UncontrollableAnger()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> creature.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void enchantedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new IsamaruHoundOfKonda());
        Permanent anger = harness.addToBattlefieldAndReturn(player1, new UncontrollableAnger());
        anger.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Enchanted creature must attack each combat if able")
    void enchantedCreatureMustAttackWhenAble() {
        Permanent creature = addCreatureReady(player1, new IsamaruHoundOfKonda());
        Permanent anger = harness.addToBattlefieldAndReturn(player1, new UncontrollableAnger());
        anger.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Summoning-sick enchanted creature is not forced to attack")
    void summoningSickCreatureIsNotForcedToAttack() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());
        Permanent anger = harness.addToBattlefieldAndReturn(player1, new UncontrollableAnger());
        anger.setAttachedTo(creature.getId());

        declareAttackers(List.of());

        assertThat(creature.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Enchanted creature is not forced to attack when tapped")
    void tappedCreatureIsNotForcedToAttack() {
        Permanent creature = addCreatureReady(player1, new IsamaruHoundOfKonda());
        creature.tap();
        Permanent anger = harness.addToBattlefieldAndReturn(player1, new UncontrollableAnger());
        anger.setAttachedTo(creature.getId());

        declareAttackers(List.of());

        assertThat(creature.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Can target a creature with Uncontrollable Anger")
    void canTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());
        harness.setHand(player1, List.of(new UncontrollableAnger()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Uncontrollable Anger can enchant an opponent's creature")
    void effectsApplyToOpponentCreature() {
        Permanent creature = addCreatureReady(player2, new IsamaruHoundOfKonda());
        harness.setHand(player1, List.of(new UncontrollableAnger()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Uncontrollable Anger stops affecting its creature when it leaves the battlefield")
    void effectsEndWhenAuraLeavesBattlefield() {
        Permanent creature = addCreatureReady(player1, new IsamaruHoundOfKonda());
        Permanent anger = harness.addToBattlefieldAndReturn(player1, new UncontrollableAnger());
        anger.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(anger);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        declareAttackers(List.of());
        assertThat(creature.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Uncontrollable Anger")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ReitoLantern());
        harness.setHand(player1, List.of(new UncontrollableAnger()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}

