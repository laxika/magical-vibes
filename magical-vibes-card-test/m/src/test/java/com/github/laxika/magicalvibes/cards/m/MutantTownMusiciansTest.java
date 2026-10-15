package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MutantTownMusicians.class, Island.class})
class MutantTownMusiciansTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 for each other creature you control that enters")
    void boostsForEachAllyCreatureEntering() {
        Permanent musicians = harness.addToBattlefieldAndReturn(player1, new MutantTownMusicians());

        harness.castFromHand(player1, new MutantTownMusicians(), "{2}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castFromHand(player1, new MutantTownMusicians(), "{2}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gqs.getEffectivePower(gd, musicians)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, musicians)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, musicians)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's creature entering")
    void doesNotBoostForOpponentCreatureEntering() {
        Permanent musicians = harness.addToBattlefieldAndReturn(player1, new MutantTownMusicians());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new MutantTownMusicians(), "{2}{R}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, musicians)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger for its own entry")
    void doesNotBoostForItsOwnEntry() {
        harness.castFromHand(player1, new MutantTownMusicians(), "{2}{R}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Permanent musicians = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, musicians)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, musicians)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost resolves on the existing musicians, not the entering creature")
    void boostsOnlyTheTriggerSourceOnResolution() {
        Permanent musicians = harness.addToBattlefieldAndReturn(player1, new MutantTownMusicians());
        harness.castFromHand(player1, new MutantTownMusicians(), "{2}{R}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Permanent newcomer = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, musicians)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, musicians)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, musicians)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, newcomer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, newcomer)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not trigger for a noncreature permanent entering")
    void doesNotBoostForLandEntering() {
        Permanent musicians = harness.addToBattlefieldAndReturn(player1, new MutantTownMusicians());
        harness.setHand(player1, List.of(new Island()));
        harness.playLand(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, musicians)).isEqualTo(2);
    }
}
