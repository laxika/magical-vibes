package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FaerieMiscreant;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrotagBugCatcher.class, SoulWarden.class, FaerieMiscreant.class, FugitiveWizard.class})
class GrotagBugCatcherTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 when it attacks as the only party creature")
    void countsItselfAsWarrior() {
        Permanent bugCatcher = addCreatureReady(player1, new GrotagBugCatcher());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bugCatcher)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bugCatcher)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets +4/+0 when it attacks with a full party")
    void boostsByPartySize() {
        Permanent bugCatcher = addCreatureReady(player1, new GrotagBugCatcher());
        harness.addToBattlefield(player1, new SoulWarden());
        harness.addToBattlefield(player1, new FaerieMiscreant());
        harness.addToBattlefield(player1, new FugitiveWizard());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bugCatcher)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bugCatcher)).isEqualTo(2);
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent bugCatcher = addCreatureReady(player1, new GrotagBugCatcher());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, bugCatcher)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bugCatcher)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bugCatcher)).isEqualTo(2);
    }
}
