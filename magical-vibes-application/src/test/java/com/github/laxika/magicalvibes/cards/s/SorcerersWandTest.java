package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GhituJourneymage;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SorcerersWandTest extends BaseCardTest {

    // ===== Equip ability =====

    @Test
    @DisplayName("Resolving equip ability attaches Sorcerer's Wand to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent wand = addWandReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(wand.getAttachedTo()).isEqualTo(creature.getId());
    }

    // ===== Granted ability: non-Wizard deals 1 damage to player =====

    @Test
    @DisplayName("Non-Wizard equipped creature deals 1 damage to target player")
    void nonWizardDeals1DamageToPlayer() {
        harness.setLife(player2, 20);

        Permanent creature = addReadyCreature(player1);
        Permanent wand = addWandReady(player1);
        wand.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(creature.isTapped()).isTrue();
    }

    // ===== Granted ability: Wizard deals 2 damage to player =====

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

    // ===== Cannot target creatures =====

    @Test
    @DisplayName("Granted ability cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = addReadyCreature(player1);
        Permanent wand = addWandReady(player1);
        wand.setAttachedTo(creature.getId());

        Permanent targetCreature = addReadyCreature(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("player or planeswalker");
    }

    // ===== Summoning sickness =====

    @Test
    @DisplayName("Summoning sick creature cannot use granted tap ability")
    void summoningSickCreatureCannotUseGrantedAbility() {
        Permanent creature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(creature);

        Permanent wand = addWandReady(player1);
        wand.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    // ===== Already tapped =====

    @Test
    @DisplayName("Already tapped creature cannot use granted tap ability")
    void tappedCreatureCannotUseGrantedAbility() {
        Permanent creature = addReadyCreature(player1);
        creature.tap();

        Permanent wand = addWandReady(player1);
        wand.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== Equipment removal =====

    @Test
    @DisplayName("Creature loses granted ability when Sorcerer's Wand is removed")
    void creatureLosesAbilityWhenRemoved() {
        Permanent creature = addReadyCreature(player1);

        Permanent wand = addWandReady(player1);
        wand.setAttachedTo(creature.getId());

        // Remove Sorcerer's Wand
        gd.playerBattlefields.get(player1.getId()).remove(wand);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    // ===== Wizard bonus uses source creature, not equipment =====

    @Test
    @DisplayName("Non-Wizard creature equipped with wand deals only 1 damage even if wand is an artifact")
    void nonWizardDealsBaseDamage() {
        harness.setLife(player2, 20);

        Permanent creature = addReadyCreature(player1);
        Permanent wand = addWandReady(player1);
        wand.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // GrizzlyBears is not a Wizard, so only 1 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    // ===== Helpers =====

    private Permanent addWandReady(Player player) {
        Permanent perm = new Permanent(new SorcerersWand());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyWizard(Player player) {
        Permanent perm = new Permanent(new GhituJourneymage());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
