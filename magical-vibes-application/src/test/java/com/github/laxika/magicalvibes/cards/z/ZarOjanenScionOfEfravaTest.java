package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZarOjanenScionOfEfrava.class, GrizzlyBears.class, HillGiant.class,
        Forest.class, Island.class, Mountain.class, Plains.class, Swamp.class})
class ZarOjanenScionOfEfravaTest extends BaseCardTest {

    @Test
    @DisplayName("When Zar Ojanen becomes tapped, it puts counters on creatures below your Domain")
    void becomingTappedPutsCountersOnCreaturesBelowDomain() {
        Permanent zar = addCreatureReady(player1, new ZarOjanenScionOfEfrava());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent hillGiant = addCreatureReady(player1, new HillGiant());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());

        tapAndResolve(zar);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(hillGiant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(zar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The Domain count is evaluated when the triggered ability resolves")
    void domainIsEvaluatedAtResolution() {
        Permanent zar = addCreatureReady(player1, new ZarOjanenScionOfEfrava());
        Permanent hillGiant = addCreatureReady(player1, new HillGiant());
        harness.addToBattlefield(player1, new Forest());

        zar.tap();
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkEnchantedPermanentTapTriggers(gd, zar));
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Plains());
        resolveAllTriggers();

        assertThat(hillGiant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking taps Zar Ojanen and triggers its ability")
    void attackingTriggersAbility() {
        Permanent zar = addCreatureReady(player1, new ZarOjanenScionOfEfrava());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        resolveAllTriggers();

        assertThat(zar.isTapped()).isTrue();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Zar Ojanen can put a counter on itself with all five basic land types")
    void includesItselfBelowDomain() {
        Permanent zar = addCreatureReady(player1, new ZarOjanenScionOfEfrava());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Swamp());

        tapAndResolve(zar);
        assertThat(zar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        zar.untap();
        tapAndResolve(zar);
        assertThat(zar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Repeated triggers use current toughness including existing counters")
    void repeatedTriggersUseCurrentToughness() {
        Permanent zar = addCreatureReady(player1, new ZarOjanenScionOfEfrava());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Plains());

        tapAndResolve(zar);
        zar.untap();
        tapAndResolve(zar);
        zar.untap();
        tapAndResolve(zar);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Duplicate land types and opposing lands do not increase your Domain")
    void duplicateAndOpposingLandTypesDoNotCount() {
        Permanent zar = addCreatureReady(player1, new ZarOjanenScionOfEfrava());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new Swamp());

        tapAndResolve(zar);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Another creature becoming tapped does not trigger Zar Ojanen")
    void otherCreatureTappingDoesNotTrigger() {
        addCreatureReady(player1, new ZarOjanenScionOfEfrava());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());

        tapAndResolve(bears);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The trigger resolves even if Zar Ojanen leaves the battlefield")
    void triggerSurvivesSourceLeaving() {
        Permanent zar = addCreatureReady(player1, new ZarOjanenScionOfEfrava());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());

        zar.tap();
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkEnchantedPermanentTapTriggers(gd, zar));
        gd.playerBattlefields.get(player1.getId()).remove(zar);
        gd.playerGraveyards.get(player1.getId()).add(zar.getCard());
        resolveAllTriggers();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void tapAndResolve(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkEnchantedPermanentTapTriggers(gd, permanent));
        resolveAllTriggers();
    }
}
