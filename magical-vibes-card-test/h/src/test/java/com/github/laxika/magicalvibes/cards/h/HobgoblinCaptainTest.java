package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BurningHands;
import com.github.laxika.magicalvibes.cards.d.DwarfholdChampion;
import com.github.laxika.magicalvibes.cards.g.GnollHunter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HobgoblinCaptain.class, DwarfholdChampion.class, GnollHunter.class, BurningHands.class})
class HobgoblinCaptainTest extends BaseCardTest {

    @Test
    void gainsFirstStrikeWhenAttackingCreaturesHaveTotalPowerAtLeastSix() {
        Permanent captain = addCreatureReady(player1, new HobgoblinCaptain());
        addCreatureReady(player1, new DwarfholdChampion());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, captain, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void doesNotGainFirstStrikeWhenAttackingCreaturesHaveTotalPowerLessThanSix() {
        Permanent captain = addCreatureReady(player1, new HobgoblinCaptain());
        addCreatureReady(player1, new GnollHunter());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, captain, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void firstStrikeWearsOffAtEndOfTurn() {
        Permanent captain = addCreatureReady(player1, new HobgoblinCaptain());
        addCreatureReady(player1, new DwarfholdChampion());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, captain, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, captain, Keyword.FIRST_STRIKE)).isFalse();
    }
    @Test
    void stillGainsFirstStrikeWhenAnotherAttackerDiesBeforeResolution() {
        Permanent captain = addCreatureReady(player1, new HobgoblinCaptain());
        Permanent companion = addCreatureReady(player1, new DwarfholdChampion());

        declareAttackers(List.of(0, 1));
        harness.setHand(player2, List.of(new BurningHands()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, companion.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Dwarfhold Champion");
        assertThat(gqs.hasKeyword(gd, captain, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void doesNotCountNonattackingCreaturesTowardTotalPower() {
        Permanent captain = addCreatureReady(player1, new HobgoblinCaptain());
        addCreatureReady(player1, new DwarfholdChampion());
        addCreatureReady(player2, new DwarfholdChampion());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, captain, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void doesNotGainFirstStrikeUnlessCaptainAttacks() {
        Permanent captain = addCreatureReady(player1, new HobgoblinCaptain());
        addCreatureReady(player1, new DwarfholdChampion());
        addCreatureReady(player1, new DwarfholdChampion());

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, captain, Keyword.FIRST_STRIKE)).isFalse();
    }
}
