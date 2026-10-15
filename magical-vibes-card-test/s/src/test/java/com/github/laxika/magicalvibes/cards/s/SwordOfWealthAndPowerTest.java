package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Deathmark;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SwordOfWealthAndPower.class, RuneclawBear.class, LightningBolt.class, Deathmark.class,
        Divination.class, Pyroclasm.class})
class SwordOfWealthAndPowerTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+2 and protection from instants and sorceries")
    void equippedCreatureGetsBoostAndProtection() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, creature, new LightningBolt())).isTrue();
        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, creature, new Deathmark())).isTrue();
    }

    @Test
    @DisplayName("Combat damage creates a Treasure and registers the next instant or sorcery copy")
    void combatDamageCreatesTreasureAndCopyTrigger() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isOne();
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("The next instant or sorcery is copied once, while a later one is not")
    void copiesOnlyTheNextInstantOrSorcery() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.setHand(player1, List.of(new Divination(), new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount).doesNotContainKey(player1.getId());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
    }

    @Test
    @DisplayName("Protection prevents instant and sorcery targets")
    void protectionPreventsInstantAndSorceryTargets() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player2, List.of(new Deathmark()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        assertThatThrownBy(() -> harness.castSorcery(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addSwordReady(Player player) {
        Permanent sword = harness.addToBattlefieldAndReturn(player, new SwordOfWealthAndPower());
        sword.setSummoningSick(false);
        return sword;
    }

    @Test
    void equipAttachesAndMovesTheBonus() {
        Permanent first = addCreatureReady(player1, new RuneclawBear());
        Permanent second = addCreatureReady(player1, new RuneclawBear());
        Permanent sword = addSwordReady(player1);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 2, null, first.getId());
        resolveAllTriggers();
        assertThat(sword.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 2, null, second.getId());
        resolveAllTriggers();
        assertThat(sword.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, first, new LightningBolt())).isFalse();
        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, second, new LightningBolt())).isTrue();
    }

    @Test
    void copiedInstantCanChooseNewTargetAfterSwordLeaves() {
        Permanent attacker = addCreatureReady(player1, new RuneclawBear());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(attacker.getId());
        Permanent first = addCreatureReady(player2, new RuneclawBear());
        Permanent second = addCreatureReady(player2, new RuneclawBear());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        gd.playerBattlefields.get(player1.getId()).remove(sword);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, first.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, second.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(first, second);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void swordControllerReceivesTreasureAndCopyWhenOpponentControlsCreature() {
        Permanent attacker = addCreatureReady(player1, new RuneclawBear());
        Permanent sword = addSwordReady(player2);
        sword.setAttachedTo(attacker.getId());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        assertThat(countPermanents(player2, "Treasure")).isOne();
        assertThat(countPermanents(player1, "Treasure")).isZero();

        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();
        harness.assertLife(player1, 14);
    }

    @Test
    void twoSwordsCopyTheSameNextSpellTwice() {
        Permanent attacker = addCreatureReady(player1, new RuneclawBear());
        addSwordReady(player1).setAttachedTo(attacker.getId());
        addSwordReady(player1).setAttachedTo(attacker.getId());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);

        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
    }

    @Test
    void unusedCopyExpiresAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new RuneclawBear());
        addSwordReady(player1).setAttachedTo(attacker.getId());
        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.setLibrary(player2, List.of(new RuneclawBear(), new RuneclawBear()));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 13);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void protectionPreventsUntargetedSorceryDamage() {
        Permanent protectedCreature = addCreatureReady(player1, new RuneclawBear());
        addSwordReady(player1).setAttachedTo(protectedCreature.getId());
        Permanent unprotectedCreature = addCreatureReady(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new Pyroclasm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(protectedCreature);
        assertThat(protectedCreature.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(unprotectedCreature);
    }
}
