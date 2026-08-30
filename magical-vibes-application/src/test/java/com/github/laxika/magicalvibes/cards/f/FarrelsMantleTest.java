package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DwarvenRuins;
import com.github.laxika.magicalvibes.cards.o.Orgg;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FarrelsMantle.class, FarrelitePriest.class, Orgg.class, DwarvenRuins.class})
class FarrelsMantleTest extends BaseCardTest {

    private Permanent addEnchantedAttacker() {
        return addEnchantedAttacker(player1, player1);
    }

    private Permanent addEnchantedAttacker(Player attackerController, Player auraController) {
        Permanent attacker = addCreatureReady(attackerController, new FarrelitePriest());
        attacker.setAttacking(true);

        Permanent aura = new Permanent(new FarrelsMantle());
        aura.setAttachedTo(attacker.getId());
        gd.playerBattlefields.get(auraController.getId()).add(aura);
        return attacker;
    }

    private Permanent addVictim() {
        return addVictim(player2);
    }

    private Permanent addVictim(Player controller) {
        return addCreatureReady(controller, new Orgg());
    }

    private void advanceToUnblockedTrigger() {
        advanceToUnblockedTrigger(player1, player2);
    }

    private void advanceToUnblockedTrigger(Player attackerController, Player defender) {
        prepareDeclareBlockers(attackerController);
        gs.declareBlockers(gd, defender, List.of());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Accepting: enchanted attacker deals power plus two damage and assigns no combat damage")
    void acceptDealsPowerPlusTwoDamageAndPreventsCombatDamage() {
        Permanent attacker = addEnchantedAttacker();
        Permanent victim = addVictim();
        harness.setLife(player2, 20);

        advanceToUnblockedTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Declining: enchanted attacker deals combat damage and the target creature is unharmed")
    void declineDealsCombatDamage() {
        addEnchantedAttacker();
        Permanent victim = addVictim();
        harness.setLife(player2, 20);

        advanceToUnblockedTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(victim.getMarkedDamage()).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Accepting with no other creature leaves combat damage intact")
    void acceptingWithNoOtherCreatureDoesNotPreventCombatDamage() {
        Permanent attacker = addEnchantedAttacker();
        harness.setLife(player2, 20);

        advanceToUnblockedTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The enchanted creature's controller makes the may choice")
    void enchantedCreatureControllerChooses() {
        Permanent attacker = addEnchantedAttacker(player2, player1);
        Permanent victim = addVictim(player1);
        harness.setLife(player1, 20);

        advanceToUnblockedTrigger(player2, player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The other target creature may be controlled by the attacker")
    void ownOtherCreatureCanBeTargeted() {
        Permanent attacker = addEnchantedAttacker();
        Permanent victim = addVictim(player1);
        harness.setLife(player2, 20);

        advanceToUnblockedTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The enchanted attacker cannot be chosen as the other target creature")
    void enchantedAttackerIsNotLegalTarget() {
        Permanent attacker = addEnchantedAttacker();
        addVictim();

        advanceToUnblockedTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The may ability cannot target a noncreature permanent")
    void noncreatureIsNotLegalTarget() {
        addEnchantedAttacker();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new DwarvenRuins());

        advanceToUnblockedTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Blocked enchanted attacker does not trigger")
    void blockedDoesNotTrigger() {
        Permanent attacker = addEnchantedAttacker();
        Permanent blocker = addVictim();

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
