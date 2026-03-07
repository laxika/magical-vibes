package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEqualToPowerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MortisDogsTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ON_ATTACK trigger with BoostSelfEffect(2, 0)")
    void hasAttackTrigger() {
        MortisDogs card = new MortisDogs();

        assertThat(card.getEffects(EffectSlot.ON_ATTACK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ATTACK).getFirst()).isInstanceOf(BoostSelfEffect.class);
        BoostSelfEffect boost = (BoostSelfEffect) card.getEffects(EffectSlot.ON_ATTACK).getFirst();
        assertThat(boost.powerBoost()).isEqualTo(2);
        assertThat(boost.toughnessBoost()).isEqualTo(0);
    }

    @Test
    @DisplayName("Has ON_DEATH trigger with TargetPlayerLosesLifeEqualToPowerEffect")
    void hasDeathTrigger() {
        MortisDogs card = new MortisDogs();

        assertThat(card.getEffects(EffectSlot.ON_DEATH)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DEATH).getFirst())
                .isInstanceOf(TargetPlayerLosesLifeEqualToPowerEffect.class);
    }

    // ===== Attack trigger =====

    @Test
    @DisplayName("Gets +2/+0 when attacking and trigger resolves")
    void boostsOnAttack() {
        Permanent dogs = addCreatureReady(player1, new MortisDogs());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(dogs.getPowerModifier()).isEqualTo(2);
        assertThat(dogs.getToughnessModifier()).isEqualTo(0);
    }

    // ===== Death trigger =====

    @Test
    @DisplayName("When Mortis Dogs dies, controller is prompted to choose a target player")
    void deathTriggerPromptsForTargetPlayer() {
        harness.addToBattlefield(player1, new MortisDogs());
        harness.setLife(player2, 20);

        setupCombatWhereMortisDogsDies();
        harness.passBothPriorities(); // Combat damage — Mortis Dogs dies

        // Mortis Dogs should be in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Mortis Dogs"));

        // Player1 should be prompted to choose a target player
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.interaction.awaitingPermanentChoicePlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Death trigger causes target player to lose life equal to base power (2) when not boosted")
    void deathTriggerLosesLifeEqualToBasePower() {
        harness.addToBattlefield(player1, new MortisDogs());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        setupCombatWhereMortisDogsDies();
        harness.passBothPriorities(); // Combat damage — Mortis Dogs dies

        // Choose opponent as target
        harness.handlePermanentChosen(player1, player2.getId());

        // Resolve the death trigger
        harness.passBothPriorities();

        // Opponent loses 2 life (base power): 20 - 2 = 18
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Death trigger uses boosted power (4) when Mortis Dogs has +2/+0 modifier")
    void deathTriggerUsesBoostedPower() {
        harness.addToBattlefield(player1, new MortisDogs());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Simulate the attack boost (+2/+0) that would have been applied by the ON_ATTACK trigger
        Permanent dogsPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Mortis Dogs"))
                .findFirst().orElseThrow();
        dogsPerm.setPowerModifier(2);

        setupCombatWhereBoostedMortisDogsDies(dogsPerm);
        harness.passBothPriorities(); // Combat damage — Mortis Dogs (4/2) dies

        // Choose opponent as target
        harness.handlePermanentChosen(player1, player2.getId());

        // Resolve the death trigger
        harness.passBothPriorities();

        // Death trigger uses boosted power (2 base + 2 modifier = 4): 20 - 4 = 16
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Death trigger can target the controller")
    void deathTriggerCanTargetSelf() {
        harness.addToBattlefield(player1, new MortisDogs());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        setupCombatWhereMortisDogsDies();
        harness.passBothPriorities(); // Combat damage — Mortis Dogs dies

        // Choose self as target
        harness.handlePermanentChosen(player1, player1.getId());

        // Resolve the death trigger
        harness.passBothPriorities();

        // Controller loses 2 life: 20 - 2 = 18
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        // Opponent unaffected
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ===== Helpers =====

    private Permanent addCreatureReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player, attackerIndices);
    }

    private void resolveAllTriggers() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }

    /**
     * Sets up combat where Mortis Dogs (player1, 2/2) attacks and is blocked by a 3/3 creature (player2).
     * Mortis Dogs will die from combat damage (does NOT get the attack boost since we skip declare attackers).
     */
    private void setupCombatWhereMortisDogsDies() {
        Permanent dogsPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Mortis Dogs"))
                .findFirst().orElseThrow();
        dogsPerm.setSummoningSick(false);
        dogsPerm.setAttacking(true);

        GrizzlyBears bigBear = new GrizzlyBears();
        bigBear.setPower(3);
        bigBear.setToughness(3);
        Permanent blockerPerm = new Permanent(bigBear);
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    /**
     * Sets up combat where a boosted Mortis Dogs (4/2) attacks and is blocked by a 5/5 creature.
     * Mortis Dogs will die from combat damage.
     */
    private void setupCombatWhereBoostedMortisDogsDies(Permanent dogsPerm) {
        dogsPerm.setSummoningSick(false);
        dogsPerm.setAttacking(true);

        GrizzlyBears bigBlocker = new GrizzlyBears();
        bigBlocker.setPower(5);
        bigBlocker.setToughness(5);
        Permanent blockerPerm = new Permanent(bigBlocker);
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }
}
