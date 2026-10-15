package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.cards.g.GhituJourneymage;
import com.github.laxika.magicalvibes.cards.b.BalothGorger;
import com.github.laxika.magicalvibes.cards.j.JayaBallard;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SorcerersWand.class, BalothGorger.class, GhituJourneymage.class, JayaBallard.class})
class SorcerersWandTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving equip ability attaches Sorcerer's Wand to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent wand = addWandReady(player1);
        Permanent creature = addCreatureReady(player1, new BalothGorger());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(wand.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Non-Wizard equipped creature deals 1 damage to target player")
    void nonWizardDeals1DamageToPlayer() {
        harness.setLife(player2, 20);

        Permanent creature = addCreatureReady(player1, new BalothGorger());
        Permanent wand = addWandReady(player1);
        wand.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Wizard equipped creature deals 2 damage to target player")
    void wizardDeals2DamageToPlayer() {
        harness.setLife(player2, 20);

        Permanent wizard = addReadyWizard(player1);
        Permanent wand = addWandReady(player1);
        wand.setAttachedTo(wizard.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(wizard.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Granted ability cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = addCreatureReady(player1, new BalothGorger());
        Permanent wand = addWandReady(player1);
        wand.setAttachedTo(creature.getId());

        Permanent targetCreature = addCreatureReady(player2, new BalothGorger());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("player or planeswalker");
    }

    @Test
    @DisplayName("Summoning sick creature cannot use granted tap ability")
    void summoningSickCreatureCannotUseGrantedAbility() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BalothGorger());

        Permanent wand = addWandReady(player1);
        wand.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Already tapped creature cannot use granted tap ability")
    void tappedCreatureCannotUseGrantedAbility() {
        Permanent creature = addCreatureReady(player1, new BalothGorger());
        creature.tap();

        Permanent wand = addWandReady(player1);
        wand.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Creature loses granted ability when Sorcerer's Wand is removed")
    void creatureLosesAbilityWhenRemoved() {
        Permanent creature = addCreatureReady(player1, new BalothGorger());

        Permanent wand = addWandReady(player1);
        wand.setAttachedTo(creature.getId());

        // Remove Sorcerer's Wand
        gd.playerBattlefields.get(player1.getId()).remove(wand);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Non-Wizard creature equipped with wand deals only 1 damage even if wand is an artifact")
    void nonWizardDealsBaseDamage() {
        harness.setLife(player2, 20);

        Permanent creature = addCreatureReady(player1, new BalothGorger());
        Permanent wand = addWandReady(player1);
        wand.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // BalothGorger is not a Wizard, so only 1 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Non-Wizard equipped creature removes one loyalty from target planeswalker")
    void nonWizardDealsDamageToPlaneswalker() {
        Permanent creature = addCreatureReady(player1, new BalothGorger());
        Permanent wand = addWandReady(player1);
        wand.setAttachedTo(creature.getId());
        Permanent jaya = harness.addToBattlefieldAndReturn(player2, new JayaBallard());
        jaya.setCounterCount(CounterType.LOYALTY, 5);

        harness.activateAbility(player1, 0, null, jaya.getId());
        harness.passBothPriorities();

        assertThat(jaya.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Wizard equipped creature removes two loyalty from target planeswalker")
    void wizardDealsDamageToPlaneswalker() {
        Permanent wizard = addReadyWizard(player1);
        Permanent wand = addWandReady(player1);
        wand.setAttachedTo(wizard.getId());
        Permanent jaya = harness.addToBattlefieldAndReturn(player2, new JayaBallard());
        jaya.setCounterCount(CounterType.LOYALTY, 5);

        harness.activateAbility(player1, 0, null, jaya.getId());
        harness.passBothPriorities();

        assertThat(jaya.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Activated ability still resolves after the Wand leaves the battlefield")
    void abilityResolvesAfterWandLeaves() {
        Permanent wizard = addReadyWizard(player1);
        Permanent wand = addWandReady(player1);
        wand.setAttachedTo(wizard.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(wand);
        gd.playerGraveyards.get(player1.getId()).add(wand.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Activated ability still deals Wizard damage after its source leaves")
    void abilityResolvesAfterWizardLeaves() {
        Permanent wizard = addReadyWizard(player1);
        Permanent wand = addWandReady(player1);
        wand.setAttachedTo(wizard.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(wizard);
        gd.playerGraveyards.get(player1.getId()).add(wizard.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentsCreature() {
        addWandReady(player1);
        Permanent creature = addCreatureReady(player2, new BalothGorger());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Granted ability can target its controller")
    void canTargetController() {
        Permanent wizard = addReadyWizard(player1);
        Permanent wand = addWandReady(player1);
        wand.setAttachedTo(wizard.getId());

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    private Permanent addWandReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new SorcerersWand());
    }

    private Permanent addReadyWizard(Player player) {
        return addCreatureReady(player, new GhituJourneymage());
    }
}
