package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KilliansConfidence.class, GrizzlyBears.class, FountainOfYouth.class})
class KilliansConfidenceTest extends BaseCardTest {

    private Card putConfidenceInGraveyard() {
        Card confidence = new KilliansConfidence();
        harness.setGraveyard(player1, List.of(confidence));
        return confidence;
    }

    private Permanent addReadyAttacker() {
        Permanent perm = addCreatureReady(player1, new GrizzlyBears());
        perm.setAttacking(true);
        return perm;
    }

    private void runCombatDamage() {
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Boosts the target creature and draws a card")
    void boostsAndDraws() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new KilliansConfidence()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.castAndResolveSorcery(player1, 0, bear.getId());

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new KilliansConfidence()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.castAndResolveSorcery(player1, 0, bear.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Fizzles and does not draw when the target leaves")
    void fizzlesWhenTargetLeaves() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new KilliansConfidence()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.castSorcery(player1, 0, bear.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("White pays the hybrid return cost")
    void whitePaysToReturnFromGraveyard() {
        Card confidence = putConfidenceInGraveyard();
        addReadyAttacker();

        runCombatDamage();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(confidence);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(confidence);
    }

    @Test
    @DisplayName("Black pays the hybrid return cost")
    void blackPaysToReturnFromGraveyard() {
        Card confidence = putConfidenceInGraveyard();
        addReadyAttacker();

        runCombatDamage();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(confidence);
    }

    @Test
    @DisplayName("Declining the return leaves the card in the graveyard")
    void decliningReturnLeavesCardInGraveyard() {
        Card confidence = putConfidenceInGraveyard();
        addReadyAttacker();

        runCombatDamage();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(confidence);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(confidence);
    }

    @Test
    @DisplayName("Multiple creatures dealing combat damage trigger only once")
    void oneOrMoreCreaturesTriggersOnlyOnce() {
        putConfidenceInGraveyard();
        addReadyAttacker();
        addReadyAttacker();

        runCombatDamage();

        assertThat(gd.pendingMayAbilities).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot return the card without white or black mana")
    void cannotReturnWithoutHybridMana() {
        Card confidence = putConfidenceInGraveyard();
        addReadyAttacker();
        runCombatDamage();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(confidence);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(confidence);
    }

    @Test
    @DisplayName("An opponent's combat damage does not trigger the return")
    void opponentsCombatDamageDoesNotTrigger() {
        Card confidence = putConfidenceInGraveyard();
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(confidence);
    }

    @Test
    @DisplayName("A card that leaves the graveyard before resolution is not returned")
    void doesNotReturnCardThatLeftGraveyard() {
        Card confidence = putConfidenceInGraveyard();
        addReadyAttacker();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(confidence));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(confidence);
        assertThat(gd.exiledCards).anySatisfy(entry -> {
            assertThat(entry.card()).isSameAs(confidence);
            assertThat(entry.ownerId()).isEqualTo(player1.getId());
        });
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new KilliansConfidence()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        Permanent fountain = gd.playerBattlefields.get(player1.getId()).get(1);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
