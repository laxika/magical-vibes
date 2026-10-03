package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BargeIn.class, YouthfulKnight.class, Gingerbrute.class})
class BargeInTest extends BaseCardTest {

    @Test
    @DisplayName("Pumps the target and grants trample to attacking non-Humans")
    void pumpsTargetAndGrantsTrampleToAttackingNonHumans() {
        Permanent target = addAttacker(player1, player2, new Gingerbrute());
        Permanent attackingHuman = addAttacker(player1, player2, new YouthfulKnight());
        Permanent idleCreature = addCreatureReady(player1, new Gingerbrute());

        harness.setHand(player1, List.of(new BargeIn()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, attackingHuman, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, idleCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void canPumpAnOpponentsAttackingHumanAndGrantTrampleToOtherAttackers() {
        Permanent human = addAttacker(player2, player1, new YouthfulKnight());
        Permanent nonHuman = addAttacker(player2, player1, new Gingerbrute());
        Permanent idleCreature = addCreatureReady(player1, new Gingerbrute());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.setHand(player1, List.of(new BargeIn()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, human.getId());

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, human, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, nonHuman, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, nonHuman)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, nonHuman)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, idleCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void rejectsNonAttackingCreature() {
        Permanent idle = addCreatureReady(player1, new Gingerbrute());
        harness.setHand(player1, List.of(new BargeIn()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, idle.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotGrantTrampleWhenTheOnlyTargetLeavesTheBattlefield() {
        Permanent target = addAttacker(player1, player2, new YouthfulKnight());
        Permanent other = addAttacker(player1, player2, new Gingerbrute());
        harness.setHand(player1, List.of(new BargeIn()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, other, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotResolveWhenTheTargetStopsAttacking() {
        Permanent target = addAttacker(player1, player2, new Gingerbrute());
        Permanent other = addAttacker(player1, player2, new Gingerbrute());
        harness.setHand(player1, List.of(new BargeIn()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.castInstant(player1, 0, target.getId());
        target.setAttacking(false);
        target.setAttackTarget(null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void bonusesExpireAtEndOfTurnAndDoNotApplyToLaterAttackers() {
        Permanent target = addAttacker(player1, player2, new Gingerbrute());
        Permanent laterAttacker = addCreatureReady(player1, new Gingerbrute());
        harness.setHand(player1, List.of(new BargeIn()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.castAndResolveInstant(player1, 0, target.getId());

        target.setAttacking(false);
        target.setAttackTarget(null);
        laterAttacker.setAttacking(true);
        laterAttacker.setAttackTarget(player2.getId());
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, laterAttacker, Keyword.TRAMPLE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addAttacker(Player controller, Player defender, Card card) {
        Permanent permanent = addCreatureReady(controller, card);
        permanent.setAttacking(true);
        permanent.setAttackTarget(defender.getId());
        return permanent;
    }
}
