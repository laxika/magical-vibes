package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.UntapSelfEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HorseshoeCrabTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Horseshoe Crab has correct card properties")
    void hasCorrectProperties() {
        HorseshoeCrab card = new HorseshoeCrab();

        assertThat(card.getName()).isEqualTo("Horseshoe Crab");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{2}{U}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLUE);
        assertThat(card.getPower()).isEqualTo(1);
        assertThat(card.getToughness()).isEqualTo(3);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.CRAB);
        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(UntapSelfEffect.class);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{U}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Horseshoe Crab puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new HorseshoeCrab()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Horseshoe Crab");
    }

    @Test
    @DisplayName("Resolving puts Horseshoe Crab onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new HorseshoeCrab()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Horseshoe Crab"));
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new HorseshoeCrab()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Activated ability - Untap self =====

    @Test
    @DisplayName("Activating ability puts UntapSelf on the stack")
    void activatingAbilityPutsOnStack() {
        addCrabReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Horseshoe Crab");
    }

    @Test
    @DisplayName("Resolving ability untaps Horseshoe Crab")
    void resolvingAbilityUntapsSelf() {
        Permanent crabPerm = addCrabReady(player1);
        crabPerm.tap();
        assertThat(crabPerm.isTapped()).isTrue();

        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(crabPerm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can activate ability when already untapped")
    void canActivateWhenAlreadyUntapped() {
        addCrabReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent crab = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(crab.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can activate ability multiple times if mana allows")
    void canActivateMultipleTimes() {
        Permanent crabPerm = addCrabReady(player1);
        crabPerm.tap();
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(crabPerm.isTapped()).isFalse();

        // Tap it again manually
        crabPerm.tap();
        assertThat(crabPerm.isTapped()).isTrue();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(crabPerm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Activating ability does NOT tap the permanent")
    void activatingAbilityDoesNotTap() {
        addCrabReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        Permanent crab = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(crab.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        addCrabReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCrabReady(player1);
        // No mana added

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability fizzles if Horseshoe Crab is removed before resolution")
    void abilityFizzlesIfSourceRemoved() {
        addCrabReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        // Remove Horseshoe Crab before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activating ability logs the activation")
    void activatingAbilityLogsActivation() {
        addCrabReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.gameLog).anyMatch(log -> log.contains("activates Horseshoe Crab's ability"));
    }

    @Test
    @DisplayName("Resolving ability logs the untap")
    void resolvingAbilityLogsUntap() {
        Permanent crabPerm = addCrabReady(player1);
        crabPerm.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("Horseshoe Crab untaps"));
    }

    // ===== Combat =====

    @Test
    @DisplayName("Unblocked Horseshoe Crab deals 1 damage to defending player")
    void dealsOneDamageWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent atkPerm = new Permanent(new HorseshoeCrab());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    // ===== Helper methods =====

    private Permanent addCrabReady(Player player) {
        HorseshoeCrab card = new HorseshoeCrab();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}

