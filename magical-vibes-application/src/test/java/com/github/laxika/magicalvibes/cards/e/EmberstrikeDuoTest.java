package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

class EmberstrikeDuoTest extends BaseCardTest {

    private Permanent addDuo() {
        harness.addToBattlefield(player1, new EmberstrikeDuo());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    @Test
    @DisplayName("Gets +1/+1 when you cast a black spell")
    void pumpsOnBlackSpell() {
        Permanent duo = addDuo();

        harness.setHand(player1, List.of(new ScatheZombies()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);

        // Cast trigger sits above the creature spell; resolve it.
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, duo)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, duo)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gains first strike when you cast a red spell")
    void gainsFirstStrikeOnRedSpell() {
        Permanent duo = addDuo();

        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, duo, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("No trigger when you cast a spell that is neither black nor red")
    void noTriggerOnGreenSpell() {
        Permanent duo = addDuo();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        // Only the creature spell is on the stack — no cast trigger.
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, duo)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, duo)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, duo, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Black-spell boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent duo = addDuo();

        harness.setHand(player1, List.of(new ScatheZombies()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, duo)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, duo)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, duo)).isEqualTo(1);
    }

    @Test
    @DisplayName("Red-spell first strike wears off at end of turn")
    void firstStrikeWearsOffAtEndOfTurn() {
        Permanent duo = addDuo();

        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, duo, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, duo, Keyword.FIRST_STRIKE)).isFalse();
    }
}
