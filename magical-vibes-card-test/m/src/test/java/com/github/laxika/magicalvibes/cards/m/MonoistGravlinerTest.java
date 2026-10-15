package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArchetypeOfFinality;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MonoistGravliner.class, GrizzlyBears.class, ArchetypeOfFinality.class})
class MonoistGravlinerTest extends BaseCardTest {

    @Test
    @DisplayName("Stationing a creature perpetually gives it deathtouch and lifelink")
    void stationingCreatureGainsKeywords() {
        Permanent gravliner = harness.addToBattlefieldAndReturn(player1, new MonoistGravliner());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(gravliner.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Six charge counters animate Monoist Gravliner and grant its keywords")
    void sixChargeCountersUnlockAbilities() {
        Permanent gravliner = harness.addToBattlefieldAndReturn(player1, new MonoistGravliner());

        gravliner.setCounterCount(CounterType.CHARGE, 5);
        assertThat(gqs.isCreature(gd, gravliner)).isFalse();
        assertThat(gqs.hasKeyword(gd, gravliner, Keyword.FLYING)).isFalse();

        gravliner.setCounterCount(CounterType.CHARGE, 6);
        assertThat(gqs.isCreature(gd, gravliner)).isTrue();
        assertThat(gqs.hasKeyword(gd, gravliner, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, gravliner, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, gravliner, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void summoningSickCreatureCanStation() {
        Permanent gravliner = harness.addToBattlefieldAndReturn(player1, new MonoistGravliner());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(gravliner.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void animatedGravlinerCannotStationItself() {
        Permanent gravliner = harness.addToBattlefieldAndReturn(player1, new MonoistGravliner());
        gravliner.setCounterCount(CounterType.CHARGE, 6);
        gravliner.setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gravliner.isTapped()).isFalse();
        assertThat(gravliner.getCounterCount(CounterType.CHARGE)).isEqualTo(6);
    }

    @Test
    void stationCannotBeActivatedDuringCombat() {
        harness.addToBattlefield(player1, new MonoistGravliner());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    void droppingBelowThresholdRemovesAnimationAndKeywords() {
        Permanent gravliner = harness.addToBattlefieldAndReturn(player1, new MonoistGravliner());
        gravliner.setCounterCount(CounterType.CHARGE, 7);
        assertThat(gqs.isCreature(gd, gravliner)).isTrue();

        gravliner.setCounterCount(CounterType.CHARGE, 5);

        assertThat(gqs.isCreature(gd, gravliner)).isFalse();
        assertThat(gqs.hasKeyword(gd, gravliner, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, gravliner, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, gravliner, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void perpetualKeywordsSurviveReturningToHandAndRecasting() {
        harness.addToBattlefield(player1, new MonoistGravliner());
        GrizzlyBears card = new GrizzlyBears();
        Permanent bears = addCreatureReady(player1, card);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, bears));
        harness.castFromHand(player1, card, "{1}{G}");
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, returned, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void prohibitedDeathtouchIsNotGrantedPerpetually() {
        harness.addToBattlefield(player1, new MonoistGravliner());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent archetype = harness.addToBattlefieldAndReturn(player2, new ArchetypeOfFinality());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, archetype));

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
    }
}
