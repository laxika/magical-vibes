package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.e.EleshNornGrandCenobite;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SigilCaptain.class, FugitiveWizard.class, GrizzlyBears.class,
        GloriousAnthem.class, EleshNornGrandCenobite.class})
class SigilCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Puts two +1/+1 counters on a 1/1 creature that enters (mandatory, no prompt)")
    void putsTwoCountersOn1_1() {
        harness.addToBattlefield(player1, new SigilCaptain());

        harness.castFromHand(player1, new FugitiveWizard(), "{U}");
        harness.passBothPriorities(); // resolve the creature spell -> it enters, Sigil Captain triggers
        harness.passBothPriorities(); // resolve Sigil Captain's mandatory trigger

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        Permanent wizard = findPermanent(player1, "Fugitive Wizard");
        assertThat(wizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wizard)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not trigger for a creature that is not 1/1")
    void noTriggerForNon1_1() {
        harness.addToBattlefield(player1, new SigilCaptain());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities(); // resolve the creature spell

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Grizzly Bears").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger for an opponent's 1/1 creature")
    void noTriggerForOpponentCreature() {
        harness.addToBattlefield(player1, new SigilCaptain());
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new FugitiveWizard(), "{U}");
        harness.passBothPriorities(); // resolve opponent's creature spell

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player2, "Fugitive Wizard").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerWhenAnthemMakesEnteringCreatureTwoTwo() {
        harness.addToBattlefield(player1, new SigilCaptain());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.castFromHand(player1, new FugitiveWizard(), "{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Fugitive Wizard")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void secondCaptainTriggerDoesNothingAfterFirstAddsCounters() {
        harness.addToBattlefield(player1, new SigilCaptain());
        harness.addToBattlefield(player1, new SigilCaptain());
        harness.castFromHand(player1, new FugitiveWizard(), "{U}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Fugitive Wizard")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void triggersForItselfWhenEnteringAsOneOne() {
        harness.addToBattlefield(player2, new EleshNornGrandCenobite());
        harness.castFromHand(player1, new SigilCaptain(), "{1}{G}{W}{W}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Sigil Captain")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
