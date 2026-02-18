package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToSelfEffect;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MantisEngineTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Mantis Engine has correct card properties")
    void hasCorrectProperties() {
        MantisEngine card = new MantisEngine();

        assertThat(card.getName()).isEqualTo("Mantis Engine");
        assertThat(card.getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(card.getManaCost()).isEqualTo("{5}");
        assertThat(card.getColor()).isNull();
        assertThat(card.getPower()).isEqualTo(3);
        assertThat(card.getToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Mantis Engine has two activated abilities")
    void hasTwoActivatedAbilities() {
        MantisEngine card = new MantisEngine();

        assertThat(card.getActivatedAbilities()).hasSize(2);

        // First ability: {2} grants flying
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{2}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(GrantKeywordToSelfEffect.class);
        assertThat(((GrantKeywordToSelfEffect) card.getActivatedAbilities().get(0).getEffects().getFirst()).keyword())
                .isEqualTo(Keyword.FLYING);

        // Second ability: {2} grants first strike
        assertThat(card.getActivatedAbilities().get(1).getManaCost()).isEqualTo("{2}");
        assertThat(card.getActivatedAbilities().get(1).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(1).isNeedsTarget()).isFalse();
        assertThat(card.getActivatedAbilities().get(1).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(1).getEffects().getFirst())
                .isInstanceOf(GrantKeywordToSelfEffect.class);
        assertThat(((GrantKeywordToSelfEffect) card.getActivatedAbilities().get(1).getEffects().getFirst()).keyword())
                .isEqualTo(Keyword.FIRST_STRIKE);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Mantis Engine puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new MantisEngine()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Mantis Engine");
    }

    @Test
    @DisplayName("Resolving puts Mantis Engine onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new MantisEngine()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Mantis Engine"));
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new MantisEngine()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Flying ability =====

    @Test
    @DisplayName("Activating flying ability puts GrantKeywordToSelf on the stack")
    void activatingFlyingPutsOnStack() {
        Permanent mantis = addMantisReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Mantis Engine");
        assertThat(entry.getTargetPermanentId()).isEqualTo(mantis.getId());
    }

    @Test
    @DisplayName("Resolving flying ability grants flying until end of turn")
    void resolvingFlyingAbilityGrantsFlying() {
        Permanent mantis = addMantisReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gs.hasKeyword(gd, mantis, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flying granted by ability resets at end of turn cleanup")
    void flyingResetsAtEndOfTurn() {
        Permanent mantis = addMantisReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gs.hasKeyword(gd, mantis, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gs.hasKeyword(gd, mantis, Keyword.FLYING)).isFalse();
    }

    // ===== First strike ability =====

    @Test
    @DisplayName("Activating first strike ability puts GrantKeywordToSelf on the stack")
    void activatingFirstStrikePutsOnStack() {
        Permanent mantis = addMantisReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Mantis Engine");
        assertThat(entry.getTargetPermanentId()).isEqualTo(mantis.getId());
    }

    @Test
    @DisplayName("Resolving first strike ability grants first strike until end of turn")
    void resolvingFirstStrikeAbilityGrantsFirstStrike() {
        Permanent mantis = addMantisReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gs.hasKeyword(gd, mantis, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("First strike granted by ability resets at end of turn cleanup")
    void firstStrikeResetsAtEndOfTurn() {
        Permanent mantis = addMantisReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gs.hasKeyword(gd, mantis, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gs.hasKeyword(gd, mantis, Keyword.FIRST_STRIKE)).isFalse();
    }

    // ===== Both abilities =====

    @Test
    @DisplayName("Can activate both abilities in the same turn")
    void canActivateBothAbilities() {
        Permanent mantis = addMantisReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gs.hasKeyword(gd, mantis, Keyword.FLYING)).isTrue();
        assertThat(gs.hasKeyword(gd, mantis, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Both keywords reset at end of turn cleanup")
    void bothKeywordsResetAtEndOfTurn() {
        Permanent mantis = addMantisReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gs.hasKeyword(gd, mantis, Keyword.FLYING)).isTrue();
        assertThat(gs.hasKeyword(gd, mantis, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gs.hasKeyword(gd, mantis, Keyword.FLYING)).isFalse();
        assertThat(gs.hasKeyword(gd, mantis, Keyword.FIRST_STRIKE)).isFalse();
    }

    // ===== Activation constraints =====

    @Test
    @DisplayName("Activating ability does NOT tap Mantis Engine")
    void activatingAbilityDoesNotTap() {
        Permanent mantis = addMantisReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(mantis.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can activate ability when tapped")
    void canActivateWhenTapped() {
        Permanent mantis = addMantisReady(player1);
        mantis.tap();
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Mantis Engine");
    }

    @Test
    @DisplayName("Can activate ability with summoning sickness")
    void canActivateWithSummoningSickness() {
        Permanent mantis = new Permanent(new MantisEngine());
        gd.playerBattlefields.get(player1.getId()).add(mantis);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Mantis Engine");
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        addMantisReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addMantisReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can activate same ability multiple times")
    void canActivateSameAbilityMultipleTimes() {
        Permanent mantis = addMantisReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gs.hasKeyword(gd, mantis, Keyword.FLYING)).isTrue();
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if Mantis Engine is removed before resolution")
    void abilityFizzlesIfSourceRemoved() {
        addMantisReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    // ===== Combat =====

    @Test
    @DisplayName("Unblocked Mantis Engine deals 3 damage to defending player")
    void dealsThreeDamageWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent mantis = addMantisReady(player1);
        mantis.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    // ===== Logging =====

    @Test
    @DisplayName("Activating ability logs the activation")
    void activatingAbilityLogsActivation() {
        addMantisReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.gameLog).anyMatch(log -> log.contains("activates Mantis Engine's ability"));
    }

    @Test
    @DisplayName("Resolving flying ability logs the keyword grant")
    void resolvingFlyingLogsGrant() {
        addMantisReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("Mantis Engine gains Flying"));
    }

    @Test
    @DisplayName("Resolving first strike ability logs the keyword grant")
    void resolvingFirstStrikeLogsGrant() {
        addMantisReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("Mantis Engine gains First strike"));
    }

    // ===== Helper methods =====

    private Permanent addMantisReady(Player player) {
        Permanent perm = new Permanent(new MantisEngine());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
