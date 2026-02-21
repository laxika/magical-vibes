package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.UntapSelfEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ColossusOfSardiaTest {

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
    @DisplayName("Colossus of Sardia has correct card properties")
    void hasCorrectProperties() {
        ColossusOfSardia card = new ColossusOfSardia();

        assertThat(card.getName()).isEqualTo("Colossus of Sardia");
        assertThat(card.getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(card.getManaCost()).isEqualTo("{9}");
        assertThat(card.getColor()).isNull();
        assertThat(card.getPower()).isEqualTo(9);
        assertThat(card.getToughness()).isEqualTo(9);
        assertThat(card.getSubtypes()).contains(CardSubtype.GOLEM);
        assertThat(card.getKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Colossus of Sardia has DoesntUntapDuringUntapStepEffect as static effect")
    void hasDoesntUntapStaticEffect() {
        ColossusOfSardia card = new ColossusOfSardia();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(DoesntUntapDuringUntapStepEffect.class);
    }

    @Test
    @DisplayName("Colossus of Sardia has activated ability with upkeep timing restriction")
    void hasActivatedAbilityWithTimingRestriction() {
        ColossusOfSardia card = new ColossusOfSardia();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{9}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).getTimingRestriction())
                .isEqualTo(ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP);
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(UntapSelfEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Colossus of Sardia puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new ColossusOfSardia()));
        harness.addMana(player1, ManaColor.WHITE, 9);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Colossus of Sardia");
    }

    @Test
    @DisplayName("Resolving Colossus of Sardia puts it on the battlefield")
    void resolvingPutsItOnBattlefield() {
        harness.setHand(player1, List.of(new ColossusOfSardia()));
        harness.addMana(player1, ManaColor.WHITE, 9);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Colossus of Sardia"));
    }

    // ===== Doesn't untap during untap step =====

    @Test
    @DisplayName("Tapped Colossus of Sardia does not untap during controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent colossusPerm = addColossusReady(player1);
        colossusPerm.tap();

        // Advance to player1's next turn (untap step)
        advanceToNextTurn(player2);

        // Colossus should still be tapped
        assertThat(colossusPerm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapped Colossus stays tapped across multiple turns")
    void staysTappedAcrossMultipleTurns() {
        Permanent colossusPerm = addColossusReady(player1);
        colossusPerm.tap();

        // Advance through player2's turn
        advanceToNextTurn(player1);
        assertThat(colossusPerm.isTapped()).isTrue();

        // Advance through player1's turn — still tapped
        advanceToNextTurn(player2);
        assertThat(colossusPerm.isTapped()).isTrue();

        // Advance through player2's turn again — still tapped
        advanceToNextTurn(player1);
        assertThat(colossusPerm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Other permanents still untap normally when Colossus is on the battlefield")
    void otherPermanentsStillUntap() {
        Permanent colossusPerm = addColossusReady(player1);
        colossusPerm.tap();

        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        bearsPerm.tap();
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        // Advance to player1's turn
        advanceToNextTurn(player2);

        // Colossus stays tapped, Bears untaps
        assertThat(colossusPerm.isTapped()).isTrue();
        assertThat(bearsPerm.isTapped()).isFalse();
    }

    // ===== Activated ability: untap during upkeep =====

    @Test
    @DisplayName("Activating untap ability during upkeep puts it on the stack")
    void activatingUntapAbilityDuringUpkeepPutsOnStack() {
        Permanent colossusPerm = addColossusReady(player1);
        colossusPerm.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 9);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Colossus of Sardia");
    }

    @Test
    @DisplayName("Resolving untap ability during upkeep untaps the Colossus")
    void resolvingUntapAbilityUntapsColossus() {
        Permanent colossusPerm = addColossusReady(player1);
        colossusPerm.tap();
        assertThat(colossusPerm.isTapped()).isTrue();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 9);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(colossusPerm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate untap ability during precombat main phase")
    void cannotActivateDuringMainPhase() {
        addColossusReady(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 9);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    @DisplayName("Cannot activate untap ability during opponent's upkeep")
    void cannotActivateDuringOpponentUpkeep() {
        addColossusReady(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 9);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    @DisplayName("Cannot activate untap ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addColossusReady(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 8);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
    }

    @Test
    @DisplayName("Cannot activate untap ability during combat")
    void cannotActivateDuringCombat() {
        addColossusReady(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 9);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    // ===== Trample =====

    @Test
    @DisplayName("Unblocked Colossus deals full 9 damage to defending player")
    void unblockedDealsFull9Damage() {
        harness.setLife(player2, 20);

        Permanent colossusPerm = new Permanent(new ColossusOfSardia());
        colossusPerm.setSummoningSick(false);
        colossusPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(colossusPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(11);
    }

    @Test
    @DisplayName("Colossus with trample deals excess damage to player when blocked by small creature")
    void tramplesToPlayerWhenBlockedBySmallCreature() {
        harness.setLife(player2, 20);

        // 9/9 Colossus attacks, blocked by 2/2 Bears
        Permanent colossusPerm = new Permanent(new ColossusOfSardia());
        colossusPerm.setSummoningSick(false);
        colossusPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(colossusPerm);

        Permanent blockerPerm = new Permanent(new GrizzlyBears());
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Trample creature blocked → assign lethal to blocker, excess to player
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blockerPerm.getId(), 2,
                player2.getId(), 7
        ));

        // 9 power - 2 toughness = 7 trample damage to player
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
        // Blocker should be dead
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Colossus with trample deals excess damage through multiple blockers")
    void tramplesToPlayerThroughMultipleBlockers() {
        harness.setLife(player2, 20);

        // 9/9 Colossus attacks, blocked by two 2/2 Bears
        Permanent colossusPerm = new Permanent(new ColossusOfSardia());
        colossusPerm.setSummoningSick(false);
        colossusPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(colossusPerm);

        Permanent blocker1 = new Permanent(new GrizzlyBears());
        blocker1.setSummoningSick(false);
        blocker1.setBlocking(true);
        blocker1.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker1);

        Permanent blocker2 = new Permanent(new GrizzlyBears());
        blocker2.setSummoningSick(false);
        blocker2.setBlocking(true);
        blocker2.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Trample creature blocked by 2 → assign lethal to each, excess to player
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker1.getId(), 2,
                blocker2.getId(), 2,
                player2.getId(), 5
        ));

        // 9 power - 2 toughness - 2 toughness = 5 trample damage to player
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        // Both blockers should be dead
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Colossus blocked by creature with equal toughness deals no trample damage")
    void noTrampleWhenBlockerHasEqualToughness() {
        harness.setLife(player2, 20);

        // 9/9 Colossus attacks, blocked by a 1/9 wall
        GrizzlyBears wall = new GrizzlyBears();
        wall.setPower(1);
        wall.setToughness(9);

        Permanent colossusPerm = new Permanent(new ColossusOfSardia());
        colossusPerm.setSummoningSick(false);
        colossusPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(colossusPerm);

        Permanent blockerPerm = new Permanent(wall);
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // 9 power - 9 toughness = 0 trample damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ===== Helpers =====

    private Permanent addColossusReady(Player player) {
        Permanent perm = new Permanent(new ColossusOfSardia());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        // Clear hands so cleanup hand-size limit doesn't interrupt turn advancement
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn (advanceTurn)
    }
}

