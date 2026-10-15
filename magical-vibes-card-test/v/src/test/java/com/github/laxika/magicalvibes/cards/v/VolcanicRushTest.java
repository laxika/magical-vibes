package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VolcanicRush.class, GrizzlyBears.class})
class VolcanicRushTest extends BaseCardTest {

    @Test
    void boostsAttackingCreaturesAndGivesThemTrample() {
        Permanent attackingCreature = addCreatureReady(player1, new GrizzlyBears());
        attackingCreature.setAttacking(true);
        Permanent bystander = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBystander = addCreatureReady(player2, new GrizzlyBears());

        castVolcanicRush();

        assertThat(attackingCreature.getEffectivePower()).isEqualTo(4);
        assertThat(attackingCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(attackingCreature.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(opposingBystander.getEffectivePower()).isEqualTo(2);
        assertThat(opposingBystander.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(bystander.getEffectivePower()).isEqualTo(2);
        assertThat(bystander.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void affectsOpponentsAttackersWhenCastByDefendingPlayer() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent defender = addCreatureReady(player1, new GrizzlyBears());

        castVolcanicRush();

        assertThat(attacker.getEffectivePower()).isEqualTo(4);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(defender.getEffectivePower()).isEqualTo(2);
        assertThat(defender.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void affectedCreaturesAreFixedAtResolution() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent laterAttacker = addCreatureReady(player1, new GrizzlyBears());

        castVolcanicRush();
        attacker.setAttacking(false);
        laterAttacker.setAttacking(true);
        Permanent newAttacker = addCreatureReady(player1, new GrizzlyBears());
        newAttacker.setAttacking(true);

        assertThat(attacker.getEffectivePower()).isEqualTo(4);
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(laterAttacker.getEffectivePower()).isEqualTo(2);
        assertThat(laterAttacker.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(newAttacker.getEffectivePower()).isEqualTo(2);
        assertThat(newAttacker.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void resolvesWithoutAttackersAndDoesNotAffectLaterAttackers() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        castVolcanicRush();
        creature.setAttacking(true);

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isFalse();
        harness.assertInGraveyard(player1, "Volcanic Rush");
    }

    @Test
    void effectWearsOffAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        castVolcanicRush();

        assertThat(attacker.getEffectivePower()).isEqualTo(4);
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(2);
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    private void castVolcanicRush() {
        harness.setHand(player1, java.util.List.of(new VolcanicRush()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0);
    }
}
