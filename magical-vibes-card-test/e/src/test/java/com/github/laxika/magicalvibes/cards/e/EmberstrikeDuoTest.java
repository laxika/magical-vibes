package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmberstrikeDuo.class, GrizzlyBears.class, HillGiant.class, ScatheZombies.class})
class EmberstrikeDuoTest extends BaseCardTest {

    private Permanent addDuo() {
        Permanent duo = harness.addToBattlefieldAndReturn(player1, new EmberstrikeDuo());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return duo;
    }

    @Test
    @DisplayName("Gets +1/+1 when you cast a black spell")
    void pumpsOnBlackSpell() {
        Permanent duo = addDuo();

        harness.castFromHand(player1, new ScatheZombies(), "{2}{B}");

        // Cast trigger sits above the creature spell; resolve it.
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, duo)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, duo)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gains first strike when you cast a red spell")
    void gainsFirstStrikeOnRedSpell() {
        Permanent duo = addDuo();

        harness.castFromHand(player1, new HillGiant(), "{3}{R}");

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, duo, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("No trigger when you cast a spell that is neither black nor red")
    void noTriggerOnGreenSpell() {
        Permanent duo = addDuo();

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

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

        harness.castFromHand(player1, new ScatheZombies(), "{2}{B}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, duo)).isEqualTo(2);

        harness.passUntil(TurnStep.END_STEP);
        assertThat(gqs.getEffectivePower(gd, duo)).isEqualTo(2);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, duo)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, duo)).isEqualTo(1);
    }

    @Test
    @DisplayName("Red-spell first strike wears off at end of turn")
    void firstStrikeWearsOffAtEndOfTurn() {
        Permanent duo = addDuo();

        harness.castFromHand(player1, new HillGiant(), "{3}{R}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, duo, Keyword.FIRST_STRIKE)).isTrue();

        harness.passUntil(TurnStep.END_STEP);
        assertThat(gqs.hasKeyword(gd, duo, Keyword.FIRST_STRIKE)).isTrue();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.hasKeyword(gd, duo, Keyword.FIRST_STRIKE)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"BLACK", "RED"})
    @DisplayName("A black-red hybrid spell triggers both abilities regardless of mana spent")
    void hybridSpellTriggersBothAbilities(ManaColor payment) {
        Permanent duo = addDuo();
        harness.setHand(player1, List.of(new EmberstrikeDuo()));
        harness.addMana(player1, payment, 2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(3);
        assertThat(gqs.getEffectivePower(gd, duo)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, duo, Keyword.FIRST_STRIKE)).isFalse();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, duo)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, duo)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, duo, Keyword.FIRST_STRIKE)).isTrue();

        harness.passBothPriorities();
        Permanent newlyCastDuo = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(newlyCastDuo.getId()).isNotEqualTo(duo.getId());
        assertThat(gqs.getEffectivePower(gd, newlyCastDuo)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, newlyCastDuo, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Each black spell adds another boost during the same turn")
    void blackSpellBoostsAccumulate() {
        Permanent duo = addDuo();
        harness.castFromHand(player1, new ScatheZombies(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castFromHand(player1, new ScatheZombies(), "{2}{B}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, duo)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, duo)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, duo, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's black-red spell triggers neither ability")
    void opponentSpellDoesNotTrigger() {
        Permanent duo = addDuo();
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new EmberstrikeDuo()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, duo)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, duo)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, duo, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Emberstrike Duo does not trigger from its own cast")
    void ownCastDoesNotTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new EmberstrikeDuo()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        Permanent duo = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, duo)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, duo)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, duo, Keyword.FIRST_STRIKE)).isFalse();
    }
}
