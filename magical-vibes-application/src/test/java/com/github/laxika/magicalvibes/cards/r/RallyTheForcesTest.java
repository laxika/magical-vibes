package com.github.laxika.magicalvibes.cards.r;

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

@CardUsed({RallyTheForces.class, GrizzlyBears.class})
class RallyTheForcesTest extends BaseCardTest {

    

    @Test
    @DisplayName("Rally the Forces boosts attacking creatures with +1/+0 and first strike")
    void boostsAttackingCreatures() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new RallyTheForces()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0);

        // Attacking creature gets +1/+0 and first strike
        assertThat(attacker.getEffectivePower()).isEqualTo(3);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
        assertThat(attacker.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();

        // Non-attacking creature is unaffected
        assertThat(nonAttacker.getEffectivePower()).isEqualTo(2);
        assertThat(nonAttacker.getEffectiveToughness()).isEqualTo(2);
        assertThat(nonAttacker.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Rally the Forces effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.setHand(player1, List.of(new RallyTheForces()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0);

        assertThat(attacker.getEffectivePower()).isEqualTo(3);
        assertThat(attacker.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(attacker.getEffectivePower()).isEqualTo(2);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
        assertThat(attacker.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }
    @Test
    void boostsOpponentsAttackersWhenCastByDefendingPlayer() {
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent defender = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RallyTheForces()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0);

        assertThat(attacker.getEffectivePower()).isEqualTo(3);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
        assertThat(attacker.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(defender.getEffectivePower()).isEqualTo(2);
        assertThat(defender.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void affectedCreaturesAreFixedAtResolution() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent laterAttacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RallyTheForces()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0);
        attacker.setAttacking(false);
        laterAttacker.setAttacking(true);

        assertThat(attacker.getEffectivePower()).isEqualTo(3);
        assertThat(attacker.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(laterAttacker.getEffectivePower()).isEqualTo(2);
        assertThat(laterAttacker.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void castingBeforeAttackersAreDeclaredDoesNotBoostLaterAttackers() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RallyTheForces()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        harness.castAndResolveInstant(player1, 0);
        creature.setAttacking(true);

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
        assertThat(creature.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        harness.assertInGraveyard(player1, "Rally the Forces");
    }
}
