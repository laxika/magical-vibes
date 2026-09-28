package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcatianSkirmishers;
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

@CardUsed({ChatzukMightyGuitarist.class, GrizzlyBears.class, IcatianSkirmishers.class})
class ChatzukMightyGuitaristTest extends BaseCardTest {

    @Test
    @DisplayName("Reduces the cost of creature spells with banding")
    void reducesBandingCreatureSpells() {
        harness.addToBattlefield(player1, new ChatzukMightyGuitarist());

        harness.castFromHand(player1, new IcatianSkirmishers(), "{1}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Icatian Skirmishers");
    }

    @Test
    @DisplayName("Does not reduce creature spells without banding")
    void doesNotReduceNonBandingCreatureSpells() {
        harness.addToBattlefield(player1, new ChatzukMightyGuitarist());

        assertThatThrownBy(() -> harness.castFromHand(player1, new GrizzlyBears(), "{G}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Boosts every creature in an attacking band by the band's size")
    void boostsCreaturesInAttackingBand() {
        Permanent chatzuk = addCreatureReady(player1, new ChatzukMightyGuitarist());
        Permanent bandmate = addCreatureReady(player1, new GrizzlyBears());

        declareAttackersInBand();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, chatzuk)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, chatzuk)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bandmate)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bandmate)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, chatzuk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, chatzuk)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bandmate)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bandmate)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost attackers that are not in a band")
    void doesNotBoostUnbandedAttackers() {
        Permanent chatzuk = addCreatureReady(player1, new ChatzukMightyGuitarist());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackersWithoutBand();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, chatzuk)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
    }

    private void declareAttackersInBand() {
        declareAttackers(List.of(0, 1), List.of(List.of(0, 1)));
    }

    private void declareAttackersWithoutBand() {
        declareAttackers(List.of(0, 1), null);
    }

    private void declareAttackers(List<Integer> attackers, List<List<Integer>> bands) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        harness.inMutationScope(() -> harness.getCombatAttackService()
                .declareAttackers(gd, player1, attackers, null, bands));
    }
}
