package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.k.KraulWarrior;
import com.github.laxika.magicalvibes.cards.s.SelesnyaGuildgate;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnflinchingCourage.class, KraulWarrior.class, SelesnyaGuildgate.class})
class UnflinchingCourageTest extends BaseCardTest {

    private Permanent attachCourage(Player controller, Permanent enchanted) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new UnflinchingCourage());
        aura.setAttachedTo(enchanted.getId());
        return aura;
    }

    @Test
    @DisplayName("Enchanted creature gets +2/+2 and has trample and lifelink")
    void grantsBoostAndKeywords() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new KraulWarrior());
        attachCourage(player1, bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Bonuses fall off when Unflinching Courage leaves the battlefield")
    void bonusesRemovedWhenAuraLeaves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new KraulWarrior());
        Permanent aura = attachCourage(player1, bears);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Unflinching Courage cannot enchant a non-creature permanent")
    void cannotEnchantNonCreature() {
        Permanent gate = harness.addToBattlefieldAndReturn(player2, new SelesnyaGuildgate());
        harness.setHand(player1, List.of(new UnflinchingCourage()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, gate.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opposing enchanted creature gains life for its controller, not the Aura controller")
    void opposingCreatureControllerGainsLife() {
        Permanent creature = addCreatureReady(player2, new KraulWarrior());
        harness.setHand(player1, List.of(new UnflinchingCourage()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Unflinching Courage");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        creature.setAttacking(true);

        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(6);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Trample damage to both a blocker and a player counts toward lifelink")
    void trampleDamageGainsLifeForAllDamageDealt() {
        Permanent attacker = addCreatureReady(player1, new KraulWarrior());
        attacker.setAttacking(true);
        attachCourage(player1, attacker);
        Permanent blocker = addCreatureReady(player2, new KraulWarrior());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 2, player2.getId(), 2));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(8);
        harness.assertInGraveyard(player2, "Kraul Warrior");
        harness.assertOnBattlefield(player1, "Unflinching Courage");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An Aura whose target leaves before resolution goes to the graveyard")
    void targetLeavingBeforeResolutionPreventsAttachment() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new KraulWarrior());
        harness.setHand(player1, List.of(new UnflinchingCourage()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Unflinching Courage");
        harness.assertNotOnBattlefield(player1, "Unflinching Courage");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Multiple Auras stack their boosts but do not multiply lifelink")
    void multipleAurasDoNotMultiplyLifelink() {
        Permanent attacker = addCreatureReady(player1, new KraulWarrior());
        attachCourage(player1, attacker);
        attachCourage(player1, attacker);
        attacker.setAttacking(true);
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The Aura is put into the graveyard when the enchanted creature leaves")
    void auraGoesToGraveyardWhenCreatureLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KraulWarrior());
        attachCourage(player2, creature);
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());

        harness.runStateBasedActions();

        harness.assertInGraveyard(player2, "Unflinching Courage");
        harness.assertNotOnBattlefield(player2, "Unflinching Courage");
    }
}
