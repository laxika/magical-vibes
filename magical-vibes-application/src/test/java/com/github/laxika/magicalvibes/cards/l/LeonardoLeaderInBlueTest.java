package com.github.laxika.magicalvibes.cards.l;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeonardoLeaderInBlue.class, GrizzlyBears.class})
class LeonardoLeaderInBlueTest extends BaseCardTest {

    @Test
    @DisplayName("Normally cast Leonardo does not boost creatures when he enters")
    void normalCastDoesNotBoostCreatures() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LeonardoLeaderInBlue()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Leonardo boosts creatures when cast for his sneak cost")
    void sneakCastBoostsCreatures() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.setHand(player1, List.of(new LeonardoLeaderInBlue()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.castWithAlternateCost(player1, 0, List.of(attacker.getId()));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        Permanent leonardo = findPermanent(player1, "Leonardo, Leader in Blue");
        assertThat(gqs.getEffectivePower(gd, leonardo)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, leonardo)).isEqualTo(1);
    }

    @Test
    @DisplayName("The activated ability grants first strike until end of turn")
    void gainsFirstStrikeUntilEndOfTurn() {
        Permanent leonardo = addCreatureReady(player1, new LeonardoLeaderInBlue());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, leonardo, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, leonardo, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Sneak returns its attacker and boosts only creatures present at resolution until cleanup")
    void sneakEntryAndBoostLifetime() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.setHand(player1, List.of(new LeonardoLeaderInBlue()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.castWithAlternateCost(player1, 0, List.of(attacker.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        harness.assertInHand(player1, "Grizzly Bears");
        harness.passBothPriorities();

        Permanent leonardo = findPermanent(player1, "Leonardo, Leader in Blue");
        assertThat(leonardo.isTapped()).isTrue();
        assertThat(leonardo.isAttacking()).isTrue();
        assertThat(leonardo.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(2);

        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, leonardo)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sneak does not allow casting for the normal mana cost during declare blockers")
    void normalCostCannotUseSneakTiming() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.setHand(player1, List.of(new LeonardoLeaderInBlue()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Leonardo, Leader in Blue");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("First strike can be activated while Leonardo is tapped and summoning sick")
    void firstStrikeHasNoTapOrSummoningSicknessRestriction() {
        Permanent leonardo = harness.addToBattlefieldAndReturn(player1, new LeonardoLeaderInBlue());
        leonardo.setSummoningSick(true);
        leonardo.tap();
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, leonardo, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ally, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(leonardo.isTapped()).isTrue();
    }
}
