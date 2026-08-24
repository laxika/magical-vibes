package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FarrelitePriest;
import com.github.laxika.magicalvibes.cards.h.Heroism;
import com.github.laxika.magicalvibes.cards.r.RiverMerfolk;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IcatianSkirmishers.class, FarrelitePriest.class, Heroism.class, RiverMerfolk.class})
class IcatianSkirmishersTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking in a band grants first strike to the other band member")
    void grantsFirstStrikeToBandmate() {
        addCreatureReady(player1, new IcatianSkirmishers());
        Permanent bandmate = addCreatureReady(player1, new FarrelitePriest());
        Permanent loner = addCreatureReady(player1, new FarrelitePriest());

        declareAttackersInBand();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bandmate, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, loner, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The trigger does not grant first strike to an attacker outside the band")
    void doesNotGrantFirstStrikeOutsideBand() {
        addCreatureReady(player1, new IcatianSkirmishers());
        Permanent attacker = addCreatureReady(player1, new FarrelitePriest());

        declareAttackersWithoutBand();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The granted first strike wears off at end of turn")
    void firstStrikeWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new IcatianSkirmishers());
        Permanent bandmate = addCreatureReady(player1, new FarrelitePriest());

        declareAttackersInBand();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bandmate, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bandmate, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The trigger still affects a bandmate if this creature leaves before resolution")
    void triggerUsesLastKnownBandMembership() {
        addCreatureReady(player1, new IcatianSkirmishers());
        Permanent bandmate = addCreatureReady(player1, new RiverMerfolk());
        harness.addToBattlefieldAndReturn(player1, new Heroism());

        declareAttackersInBand();
        harness.activateAbility(player1, 2, null, null);
        harness.assertNotOnBattlefield(player1, "Icatian Skirmishers");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bandmate, Keyword.FIRST_STRIKE)).isTrue();
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
