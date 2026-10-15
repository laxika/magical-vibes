package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodlinePretender.class, FugitiveWizard.class, GrizzlyBears.class})
class BloodlinePretenderTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a creature type as it enters sets chosenSubtype on the permanent")
    void choosingSubtypeSetsOnPermanent() {
        harness.castFromHand(player1, new BloodlinePretender(), "{3}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WIZARD");

        assertThat(findPermanent(player1, "Bloodline Pretender").getChosenSubtype())
                .isEqualTo(CardSubtype.WIZARD);
    }

    @Test
    @DisplayName("A creature you control of the chosen type puts a counter on Bloodline Pretender")
    void chosenTypeCreaturePutsCounterOnPretender() {
        Permanent pretender = addPretender(player1, CardSubtype.WIZARD);

        harness.castFromHand(player1, new FugitiveWizard(), "{U}");
        resolveAllTriggers();

        assertThat(pretender.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Fugitive Wizard")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A creature of a different type does not trigger Bloodline Pretender")
    void otherTypeDoesNotTrigger() {
        Permanent pretender = addPretender(player1, CardSubtype.WIZARD);

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(pretender.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A matching creature entering under an opponent's control does not trigger Bloodline Pretender")
    void opponentCreatureDoesNotTrigger() {
        Permanent pretender = addPretender(player1, CardSubtype.WIZARD);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new FugitiveWizard(), "{U}");
        harness.passBothPriorities();

        assertThat(pretender.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("No creature type choice means no creature-enter trigger")
    void noCounterWithoutChoice() {
        Permanent pretender = harness.addToBattlefieldAndReturn(player1, new BloodlinePretender());

        harness.castFromHand(player1, new FugitiveWizard(), "{U}");
        harness.passBothPriorities();

        assertThat(pretender.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodline Pretender does not trigger for its own entry")
    void ownEntryDoesNotPutCounterOnPretender() {
        harness.castFromHand(player1, new BloodlinePretender(), "{3}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WIZARD");
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Bloodline Pretender")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Another changeling matches the chosen type regardless of its own choice")
    void anotherChangelingTriggersExistingPretender() {
        Permanent first = addPretender(player1, CardSubtype.WIZARD);

        harness.castFromHand(player1, new BloodlinePretender(), "{3}");
        harness.passBothPriorities();
        Permanent second = gd.playerBattlefields.get(player1.getId()).getLast();
        harness.handleListChoice(player1, "BEAR");
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Each matching creature entry adds another counter")
    void matchingEntriesAccumulateCounters() {
        Permanent pretender = addPretender(player1, CardSubtype.WIZARD);

        harness.castFromHand(player1, new FugitiveWizard(), "{U}");
        resolveAllTriggers();
        harness.castFromHand(player1, new FugitiveWizard(), "{U}");
        resolveAllTriggers();

        assertThat(pretender.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private Permanent addPretender(Player player, CardSubtype chosen) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new BloodlinePretender());
        permanent.setChosenSubtype(chosen);
        return permanent;
    }
}
