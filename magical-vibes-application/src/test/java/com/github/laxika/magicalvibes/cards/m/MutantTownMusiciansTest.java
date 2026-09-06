package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MutantTownMusicians.class, FugitiveWizard.class})
class MutantTownMusiciansTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 for each other creature you control that enters")
    void boostsForEachAllyCreatureEntering() {
        Permanent musicians = harness.addToBattlefieldAndReturn(player1, new MutantTownMusicians());

        harness.setHand(player1, List.of(new FugitiveWizard(), new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
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
        harness.setHand(player2, List.of(new FugitiveWizard()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, musicians)).isEqualTo(2);
    }
}
