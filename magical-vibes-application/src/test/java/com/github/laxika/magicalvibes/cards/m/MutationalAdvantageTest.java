package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MutationalAdvantage.class, Forest.class, GrizzlyBears.class, Pyroclasm.class})
class MutationalAdvantageTest extends BaseCardTest {

    @Test
    @DisplayName("Protects your countered permanents and proliferates")
    void protectsCounteredPermanentsAndProliferates() {
        Permanent counteredCreature = addCounteredBear(player1);
        Permanent counteredLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        counteredLand.setCounterCount(CounterType.CHARGE, 1);
        Permanent uncounteredCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = addCounteredBear(player2);

        cast();

        assertThat(gqs.hasKeyword(gd, counteredCreature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, counteredCreature, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, counteredLand, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, counteredLand, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, uncounteredCreature, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HEXPROOF)).isFalse();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds())
                .contains(counteredCreature.getId(), counteredLand.getId(), opponentCreature.getId())
                .doesNotContain(uncounteredCreature.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(counteredCreature.getId(), counteredLand.getId()));

        assertThat(counteredCreature.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(counteredLand.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(opponentCreature.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevents damage to countered permanents you control")
    void preventsDamageToCounteredPermanentsYouControl() {
        Permanent protectedCreature = addCounteredBear(player1);
        Permanent unprotectedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast();
        harness.handleMultiplePermanentsChosen(player1, List.of(protectedCreature.getId()));

        harness.castFromHand(player1, new Pyroclasm(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(protectedCreature.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(protectedCreature)
                .doesNotContain(unprotectedCreature);
    }

    @Test
    @DisplayName("Protection keywords expire at end of turn")
    void protectionExpiresAtEndOfTurn() {
        Permanent protectedCreature = addCounteredBear(player1);

        cast();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gqs.hasKeyword(gd, protectedCreature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, protectedCreature, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, protectedCreature, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, protectedCreature, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Damage protection remains after all counters are removed")
    void damageProtectionRemainsAfterCountersAreRemoved() {
        Permanent protectedCreature = addCounteredBear(player1);

        cast();
        harness.handleMultiplePermanentsChosen(player1, List.of());
        protectedCreature.setCounterCount(CounterType.CHARGE, 0);

        harness.castFromHand(player1, new Pyroclasm(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, protectedCreature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, protectedCreature, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(protectedCreature.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(protectedCreature);
    }

    @Test
    @DisplayName("A permanent that gains its first counter later is not protected")
    void gainingFirstCounterLaterDoesNotGrantProtection() {
        Permanent initiallyCountered = addCounteredBear(player1);
        Permanent unprotectedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast();
        harness.handleMultiplePermanentsChosen(player1, List.of());
        unprotectedCreature.setCounterCount(CounterType.CHARGE, 1);

        assertThat(gqs.hasKeyword(gd, unprotectedCreature, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, unprotectedCreature, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.castFromHand(player1, new Pyroclasm(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(initiallyCountered)
                .doesNotContain(unprotectedCreature);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Countered permanents entering later are not protected")
    void counteredPermanentEnteringLaterIsNotProtected() {
        Permanent initiallyCountered = addCounteredBear(player1);

        cast();
        harness.handleMultiplePermanentsChosen(player1, List.of());
        Permanent laterCreature = addCounteredBear(player1);

        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.castFromHand(player1, new Pyroclasm(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(initiallyCountered)
                .doesNotContain(laterCreature);
    }

    @Test
    @DisplayName("Proliferates every counter kind on selected opposing permanents and players")
    void proliferatesEveryCounterKindOnSelectedPermanentsAndPlayers() {
        Permanent opponentCreature = addCounteredBear(player2);
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        gd.playerPoisonCounters.put(player1.getId(), 1);
        gd.playerEnergyCounters.put(player1.getId(), 2);
        gd.playerRadCounters.put(player2.getId(), 1);
        gd.playerExperienceCounters.put(player2.getId(), 3);

        cast();
        harness.handleMultiplePermanentsChosen(player1,
                List.of(opponentCreature.getId(), player1.getId(), player2.getId()));

        assertThat(opponentCreature.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.playerExperienceCounters.get(player2.getId())).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Resolves without a proliferate choice when nothing has counters")
    void resolvesWhenNothingHasCounters() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.assertInGraveyard(player1, "Mutational Advantage");
    }

    @Test
    @DisplayName("Damage prevention does not protect opposing countered permanents")
    void damagePreventionDoesNotProtectOpponents() {
        Permanent ownCreature = addCounteredBear(player1);
        Permanent opponentCreature = addCounteredBear(player2);

        cast();
        harness.handleMultiplePermanentsChosen(player1, List.of(opponentCreature.getId()));
        harness.castFromHand(player1, new Pyroclasm(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Damage prevention expires at end of turn")
    void damagePreventionExpiresAtEndOfTurn() {
        Permanent protectedCreature = addCounteredBear(player1);

        cast();
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new Pyroclasm(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(protectedCreature);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private Permanent addCounteredBear(Player player) {
        Permanent bear = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        bear.setCounterCount(CounterType.CHARGE, 1);
        return bear;
    }

    private void cast() {
        harness.castFromHand(player1, new MutationalAdvantage(), "{1}{G}{U}");
        harness.passBothPriorities();
    }
}
