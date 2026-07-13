package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GravelgillDuoTest extends BaseCardTest {

    private Permanent addDuo() {
        harness.addToBattlefield(player1, new GravelgillDuo());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    @Test
    @DisplayName("Gets +1/+1 when you cast a blue spell")
    void pumpsOnBlueSpell() {
        Permanent duo = addDuo();

        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);

        // Cast trigger sits above the creature spell; resolve it.
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, duo)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, duo)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gains fear when you cast a black spell")
    void gainsFearOnBlackSpell() {
        Permanent duo = addDuo();

        harness.setHand(player1, List.of(new ScatheZombies()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, duo, Keyword.FEAR)).isTrue();
    }

    @Test
    @DisplayName("No trigger when you cast a spell that is neither blue nor black")
    void noTriggerOnGreenSpell() {
        Permanent duo = addDuo();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        // Only the creature spell is on the stack — no cast trigger.
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, duo)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, duo)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, duo, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("Blue-spell boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent duo = addDuo();

        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, duo)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, duo)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, duo)).isEqualTo(1);
    }

    @Test
    @DisplayName("Black-spell fear wears off at end of turn")
    void fearWearsOffAtEndOfTurn() {
        Permanent duo = addDuo();

        harness.setHand(player1, List.of(new ScatheZombies()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, duo, Keyword.FEAR)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, duo, Keyword.FEAR)).isFalse();
    }
}
