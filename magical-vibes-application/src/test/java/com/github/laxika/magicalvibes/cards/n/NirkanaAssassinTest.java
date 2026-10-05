package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.s.StoneHavenMedic;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NirkanaAssassin.class, StoneHavenMedic.class})
class NirkanaAssassinTest extends BaseCardTest {

    @Test
    @DisplayName("Gains deathtouch when its controller gains life")
    void gainsDeathtouchOnControllerLifeGain() {
        Permanent assassin = harness.addToBattlefieldAndReturn(player1, new NirkanaAssassin());
        addCreatureReady(player1, new StoneHavenMedic());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(assassin.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Does not trigger when an opponent gains life")
    void doesNotTriggerOnOpponentLifeGain() {
        Permanent assassin = harness.addToBattlefieldAndReturn(player1, new NirkanaAssassin());
        addCreatureReady(player2, new StoneHavenMedic());
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(assassin.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Deathtouch wears off at end of turn")
    void deathtouchWearsOffAtEndOfTurn() {
        Permanent assassin = harness.addToBattlefieldAndReturn(player1, new NirkanaAssassin());
        addCreatureReady(player1, new StoneHavenMedic());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();
        assertThat(assassin.hasKeyword(Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(assassin.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Life gain queues deathtouch rather than granting it immediately")
    void deathtouchWaitsForTriggerResolution() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            Permanent assassin = harness.addToBattlefieldAndReturn(player1, new NirkanaAssassin());
            addCreatureReady(player1, new StoneHavenMedic());
            harness.addMana(player1, ManaColor.WHITE, 1);

            harness.activateAbility(player1, 1, null, null);
            assertThat(assassin.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
            harness.passBothPriorities();

            harness.assertLife(player1, 21);
            assertThat(gd.stack).hasSize(1);
            assertThat(assassin.hasKeyword(Keyword.DEATHTOUCH)).isFalse();

            resolveAllTriggers();
            assertThat(assassin.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        });
    }

    @Test
    @DisplayName("Each controlled Assassin triggers independently, without granting deathtouch to other creatures")
    void eachAssassinGainsDeathtouch() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            Permanent first = harness.addToBattlefieldAndReturn(player1, new NirkanaAssassin());
            Permanent second = harness.addToBattlefieldAndReturn(player1, new NirkanaAssassin());
            Permanent medic = addCreatureReady(player1, new StoneHavenMedic());
            harness.addMana(player1, ManaColor.WHITE, 1);

            harness.activateAbility(player1, 2, null, null);
            harness.passBothPriorities();
            assertThat(gd.stack).hasSize(2);
            resolveAllTriggers();

            assertThat(first.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
            assertThat(second.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
            assertThat(medic.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
        });
    }

    @Test
    @DisplayName("Further life gain still triggers after deathtouch has already been granted")
    void triggersOnEveryLifeGain() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            Permanent assassin = harness.addToBattlefieldAndReturn(player1, new NirkanaAssassin());
            addCreatureReady(player1, new StoneHavenMedic());
            addCreatureReady(player1, new StoneHavenMedic());
            harness.addMana(player1, ManaColor.WHITE, 2);

            harness.activateAbility(player1, 1, null, null);
            resolveAllTriggers();
            assertThat(assassin.hasKeyword(Keyword.DEATHTOUCH)).isTrue();

            harness.activateAbility(player1, 2, null, null);
            harness.passBothPriorities();
            harness.assertLife(player1, 22);
            assertThat(gd.stack).hasSize(1);
            resolveAllTriggers();

            assertThat(assassin.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();
            assertThat(assassin.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
        });
    }
}
