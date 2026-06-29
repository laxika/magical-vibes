package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantDamageToOpponentCreatureBounceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ArmWithAetherTest extends BaseCardTest {

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void castAndResolveArmWithAether() {
        harness.setHand(player1, List.of(new ArmWithAether()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Arm with Aether has correct effects")
    void hasCorrectEffects() {
        ArmWithAether card = new ArmWithAether();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(GrantDamageToOpponentCreatureBounceUntilEndOfTurnEffect.class);
    }

    // ===== Sorcery resolution =====

    @Test
    @DisplayName("Casting Arm with Aether grants bounce ability to all controlled creatures")
    void grantsAbilityToControlledCreatures() {
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        Permanent elves = addReadyCreature(player1, new LlanowarElves());

        castAndResolveArmWithAether();

        assertThat(bears.isHasDamageToOpponentCreatureBounce()).isTrue();
        assertThat(elves.isHasDamageToOpponentCreatureBounce()).isTrue();
    }

    @Test
    @DisplayName("Arm with Aether does not affect opponent's creatures")
    void doesNotAffectOpponentCreatures() {
        addReadyCreature(player1, new GrizzlyBears());
        Permanent opponentBears = addReadyCreature(player2, new GrizzlyBears());

        castAndResolveArmWithAether();

        assertThat(opponentBears.isHasDamageToOpponentCreatureBounce()).isFalse();
    }

    // ===== Combat damage trigger =====

    @Test
    @DisplayName("Creature with granted ability triggers bounce on combat damage to player")
    void triggersBounceOnCombatDamage() {
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setHasDamageToOpponentCreatureBounce(true);
        attacker.setAttacking(true);
        addReadyCreature(player2, new GrizzlyBears());

        resolveCombat();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_PERMANENT_CHOICE);
        assertThat(gd.interaction.multiPermanentChoiceContext()).isNotNull();
        assertThat(gd.interaction.multiPermanentChoiceContext().playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.multiSelection().multiPermanentMaxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Selecting a creature to bounce returns it to owner's hand")
    void bouncesSelectedCreature() {
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setHasDamageToOpponentCreatureBounce(true);
        attacker.setAttacking(true);
        Permanent target = addReadyCreature(player2, new GrizzlyBears());

        resolveCombat();

        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Selecting zero creatures is allowed (may ability)")
    void mayDeclineBounce() {
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setHasDamageToOpponentCreatureBounce(true);
        attacker.setAttacking(true);
        addReadyCreature(player2, new GrizzlyBears());

        resolveCombat();

        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Bounce only targets creatures, not non-creature permanents")
    void onlyTargetsCreatures() {
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setHasDamageToOpponentCreatureBounce(true);
        attacker.setAttacking(true);
        // Add a non-creature permanent (land) to opponent's battlefield
        Permanent land = new Permanent(new com.github.laxika.magicalvibes.cards.f.Forest());
        land.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(land);

        resolveCombat();

        // Should report no creatures (land is not a creature)
        assertThat(gd.gameLog).anyMatch(log -> log.contains("has no creatures"));
    }

    @Test
    @DisplayName("No trigger when attacker is blocked and deals no damage to player")
    void noTriggerWhenBlocked() {
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setHasDamageToOpponentCreatureBounce(true);
        attacker.setAttacking(true);
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.MULTI_PERMANENT_CHOICE);
    }

    @Test
    @DisplayName("Creature without granted ability does not trigger bounce")
    void noTriggerWithoutGrantedAbility() {
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        // NOT setting hasDamageToOpponentCreatureBounce
        attacker.setAttacking(true);
        addReadyCreature(player2, new GrizzlyBears());

        resolveCombat();

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.MULTI_PERMANENT_CHOICE);
    }

    // ===== Full integration: cast sorcery then attack =====

    @Test
    @DisplayName("Full flow: cast Arm with Aether, attack, bounce opponent creature")
    void fullFlow() {
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        Permanent opponentCreature = addReadyCreature(player2, new GrizzlyBears());

        castAndResolveArmWithAether();

        assertThat(attacker.isHasDamageToOpponentCreatureBounce()).isTrue();

        // Now attack
        attacker.setAttacking(true);
        resolveCombat();

        // Should prompt for creature bounce
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_PERMANENT_CHOICE);

        // Choose the opponent's creature
        harness.handleMultiplePermanentsChosen(player1, List.of(opponentCreature.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(opponentCreature.getId()));
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }
}
