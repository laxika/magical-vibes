package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetOpponentOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BurningSunsAvatarTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Burning Sun's Avatar has correct ETB effects")
    void hasCorrectEffects() {
        BurningSunsAvatar card = new BurningSunsAvatar();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).get(0))
                .isInstanceOf(DealDamageToTargetOpponentOrPlaneswalkerEffect.class);
        assertThat(((DealDamageToTargetOpponentOrPlaneswalkerEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).get(0)).damage())
                .isEqualTo(3);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).get(1))
                .isInstanceOf(DealDamageToTargetCreatureEffect.class);
        assertThat(((DealDamageToTargetCreatureEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).get(1)).damage())
                .isEqualTo(3);
    }

    @Test
    @DisplayName("Has correct target configuration — mandatory opponent/planeswalker + optional creature")
    void hasCorrectTargetConfig() {
        BurningSunsAvatar card = new BurningSunsAvatar();

        assertThat(card.getSpellTargets()).hasSize(2);
        // First target: mandatory (1-1) opponent or planeswalker
        assertThat(card.getSpellTargets().get(0).getMinTargets()).isEqualTo(1);
        assertThat(card.getSpellTargets().get(0).getMaxTargets()).isEqualTo(1);
        // Second target: optional (0-1) creature
        assertThat(card.getSpellTargets().get(1).getMinTargets()).isEqualTo(0);
        assertThat(card.getSpellTargets().get(1).getMaxTargets()).isEqualTo(1);
    }

    // ===== ETB deals damage to opponent and creature =====

    @Test
    @DisplayName("ETB deals 3 damage to opponent and 3 damage to target creature")
    void etbDeals3DamageToOpponentAnd3DamageToCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BurningSunsAvatar()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.setLife(player2, 20);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        gs.playCard(gd, player1, 0, 0, null, null, List.of(player2.getId(), creatureId), List.of());

        // Resolve creature spell → enters battlefield, ETB triggers
        harness.passBothPriorities();
        // Resolve ETB triggered ability
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        // Opponent takes 3 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        // Grizzly Bears (2/2) takes 3 damage and dies
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== ETB deals damage to opponent only (no creature target) =====

    @Test
    @DisplayName("ETB deals 3 damage to opponent when no creature is targeted")
    void etbDeals3DamageToOpponentOnly() {
        harness.setHand(player1, List.of(new BurningSunsAvatar()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.setLife(player2, 20);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(player2.getId()), List.of());

        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB triggered ability
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    // ===== Creature damage doesn't kill a big creature =====

    @Test
    @DisplayName("ETB deals 3 damage to a 4/4 creature but does not kill it")
    void etbDeals3DamageDoesNotKillBigCreature() {
        GrizzlyBears bigCreature = new GrizzlyBears();
        bigCreature.setPower(4);
        bigCreature.setToughness(4);
        harness.addToBattlefield(player2, bigCreature);
        harness.setHand(player1, List.of(new BurningSunsAvatar()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.setLife(player2, 20);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        gs.playCard(gd, player1, 0, 0, null, null, List.of(player2.getId(), creatureId), List.of());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        // 4/4 creature survives 3 damage
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== ETB trigger goes on stack =====

    @Test
    @DisplayName("Resolving creature puts ETB triggered ability on the stack")
    void resolvingCreaturePutsEtbOnStack() {
        harness.setHand(player1, List.of(new BurningSunsAvatar()));
        harness.addMana(player1, ManaColor.RED, 6);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(player2.getId()), List.of());

        // Resolve creature spell → enters battlefield, ETB triggers
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Burning Sun's Avatar"));
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Burning Sun's Avatar");
    }

    // ===== Creature enters battlefield even without targets =====

    @Test
    @DisplayName("Creature enters battlefield when cast without targets")
    void entersWithoutTargets() {
        harness.setHand(player1, List.of(new BurningSunsAvatar()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);

        // Resolve creature spell
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Burning Sun's Avatar"));
    }
}
