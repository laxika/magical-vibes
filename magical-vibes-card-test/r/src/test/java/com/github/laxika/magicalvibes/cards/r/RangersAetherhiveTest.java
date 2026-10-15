package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AfterburnerExpert;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RangersAetherhive.class, AfterburnerExpert.class, GrizzlyBears.class})
@DisplayName("Rangers' Aetherhive")
class RangersAetherhiveTest extends BaseCardTest {

    @Test
    @DisplayName("Activating an exhaust ability creates a Thopter")
    void activatingExhaustAbilityCreatesThopter() {
        addReadyAetherhive();
        addCreatureReady(player1, new AfterburnerExpert());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Thopter")).hasSize(1);
        assertThat(findPermanents(player1, "Thopter").getFirst().getCard().getKeywords())
                .contains(Keyword.FLYING);
        assertThat(findPermanents(player1, "Thopter").getFirst().getCard().getAdditionalTypes())
                .contains(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Crew does not trigger Thopter creation")
    void crewDoesNotTriggerThopterCreation() {
        Permanent aetherhive = addReadyAetherhive();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Thopter")).isEmpty();
        assertThat(gqs.isCreature(gd, aetherhive)).isTrue();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's exhaust activation does not create a Thopter")
    void opponentsExhaustAbilityDoesNotTrigger() {
        addReadyAetherhive();
        harness.addToBattlefield(player2, new AfterburnerExpert());
        harness.addMana(player2, ManaColor.GREEN, 4);
        harness.forceActivePlayer(player2);

        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Thopter")).isEmpty();
        assertThat(findPermanents(player2, "Thopter")).isEmpty();
    }

    @Test
    @DisplayName("Each Aetherhive triggers while it is an uncrewed Vehicle")
    void eachUncrewedAetherhiveTriggers() {
        harness.addToBattlefield(player1, new RangersAetherhive());
        harness.addToBattlefield(player1, new RangersAetherhive());
        harness.addToBattlefield(player1, new AfterburnerExpert());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 2, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Thopter")).hasSize(2);
    }

    @Test
    @DisplayName("Exhaust trigger resolves before the activated ability and creates a colorless 1/1")
    void triggerResolvesBeforeExhaustAbility() {
        addReadyAetherhive();
        Permanent expert = harness.addToBattlefieldAndReturn(player1, new AfterburnerExpert());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        Permanent thopter = findPermanent(player1, "Thopter");
        assertThat(gqs.isCreature(gd, thopter)).isTrue();
        assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(1);
        assertThat(thopter.getCard().getColors()).isEmpty();
        assertThat(thopter.getCard().getColor()).isNull();
        assertThat(thopter.isTapped()).isFalse();
        assertThat(expert.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(expert.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanents(player1, "Thopter")).hasSize(1);
    }

    @Test
    @DisplayName("A summoning-sick creature can crew the Vehicle")
    void summoningSickCreatureCanCrew() {
        Permanent aetherhive = harness.addToBattlefieldAndReturn(player1, new RangersAetherhive());
        Permanent expert = harness.addToBattlefieldAndReturn(player1, new AfterburnerExpert());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(expert.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, aetherhive)).isTrue();
        assertThat(findPermanents(player1, "Thopter")).isEmpty();
    }

    @Test
    @DisplayName("A crewed Aetherhive attacks without tapping and stops being a creature next turn")
    void vigilanceAndCrewDuration() {
        Permanent aetherhive = addReadyAetherhive();
        harness.addToBattlefield(player1, new AfterburnerExpert());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));

        assertThat(aetherhive.isTapped()).isFalse();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, aetherhive)).isFalse();
    }

    private Permanent addReadyAetherhive() {
        return addCreatureReady(player1, new RangersAetherhive());
    }
}
