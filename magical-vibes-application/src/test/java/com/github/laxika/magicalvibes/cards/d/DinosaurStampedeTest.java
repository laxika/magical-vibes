package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DinosaurStampede.class, DeathgorgeScavenger.class, GrizzlyBears.class})
class DinosaurStampedeTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking Dinosaur gets +2/+0 and trample")
    void attackingDinosaurGetsBothEffects() {
        Permanent dino = addCreatureReady(player1, new DeathgorgeScavenger()); // 3/2 Dinosaur
        dino.setAttacking(true);

        harness.setHand(player1, List.of(new DinosaurStampede()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0);

        assertThat(dino.getEffectivePower()).isEqualTo(5);  // 3 + 2
        assertThat(dino.getEffectiveToughness()).isEqualTo(2);
        assertThat(dino.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Attacking non-Dinosaur gets +2/+0 but not trample")
    void attackingNonDinosaurGetsBoostOnly() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears()); // 2/2 Bear
        bear.setAttacking(true);

        harness.setHand(player1, List.of(new DinosaurStampede()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0);

        assertThat(bear.getEffectivePower()).isEqualTo(4);  // 2 + 2
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
        assertThat(bear.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Non-attacking Dinosaur gets trample but not +2/+0")
    void nonAttackingDinosaurGetsTrampleOnly() {
        Permanent dino = addCreatureReady(player1, new DeathgorgeScavenger()); // 3/2 Dinosaur

        harness.setHand(player1, List.of(new DinosaurStampede()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0);

        assertThat(dino.getEffectivePower()).isEqualTo(3);  // unchanged
        assertThat(dino.getEffectiveToughness()).isEqualTo(2);
        assertThat(dino.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Non-attacking non-Dinosaur gets nothing")
    void nonAttackingNonDinosaurGetsNothing() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears()); // 2/2 Bear

        harness.setHand(player1, List.of(new DinosaurStampede()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0);

        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
        assertThat(bear.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Opponent's Dinosaurs do not gain trample")
    void opponentDinosaursDoNotGainTrample() {
        Permanent ownDino = addCreatureReady(player1, new DeathgorgeScavenger());
        ownDino.setAttacking(true);
        Permanent opponentDino = addCreatureReady(player2, new DeathgorgeScavenger());

        harness.setHand(player1, List.of(new DinosaurStampede()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0);

        assertThat(ownDino.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(opponentDino.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent dino = addCreatureReady(player1, new DeathgorgeScavenger());
        dino.setAttacking(true);

        harness.setHand(player1, List.of(new DinosaurStampede()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0);

        assertThat(dino.getEffectivePower()).isEqualTo(5);
        assertThat(dino.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(dino.getEffectivePower()).isEqualTo(3);
        assertThat(dino.getEffectiveToughness()).isEqualTo(2);
        assertThat(dino.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Defending caster boosts opposing attackers but only grants trample to own Dinosaurs")
    void defendingCasterBoostsOpposingAttackers() {
        Permanent attacker = addCreatureReady(player2, new DeathgorgeScavenger());
        attacker.setAttacking(true);
        Permanent ownDinosaur = addCreatureReady(player1, new DeathgorgeScavenger());

        harness.setHand(player1, List.of(new DinosaurStampede()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0);

        assertThat(attacker.getEffectivePower()).isEqualTo(5);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(ownDinosaur.getEffectivePower()).isEqualTo(3);
        assertThat(ownDinosaur.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Creatures entering after resolution do not receive either effect")
    void laterCreaturesDoNotReceiveEffects() {
        harness.setHand(player1, List.of(new DinosaurStampede()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveInstant(player1, 0);

        Permanent dinosaur = addCreatureReady(player1, new DeathgorgeScavenger());
        dinosaur.setAttacking(true);

        assertThat(dinosaur.getEffectivePower()).isEqualTo(3);
        assertThat(dinosaur.getEffectiveToughness()).isEqualTo(2);
        assertThat(dinosaur.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("A creature that starts attacking after resolution does not receive the boost")
    void laterAttackerDoesNotReceiveBoost() {
        Permanent dinosaur = addCreatureReady(player1, new DeathgorgeScavenger());
        harness.setHand(player1, List.of(new DinosaurStampede()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveInstant(player1, 0);
        dinosaur.setAttacking(true);

        assertThat(dinosaur.getEffectivePower()).isEqualTo(3);
        assertThat(dinosaur.getEffectiveToughness()).isEqualTo(2);
        assertThat(dinosaur.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }
}
