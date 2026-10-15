package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.Expansion;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnbreakableFormation.class, GrizzlyBears.class, Expansion.class})
class UnbreakableFormationTest extends BaseCardTest {

    @Test
    void addendumStrengthensAllOwnCreaturesDuringMainPhase() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UnbreakableFormation()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(ownBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownBear.getEffectivePower()).isEqualTo(3);
        assertThat(ownBear.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.VIGILANCE)).isTrue();
        assertThat(opponentBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, opponentBear, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentBear, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void outsideMainPhaseOnlyGrantsIndestructible() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new UnbreakableFormation()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(ownBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void temporaryKeywordsWearOffAtEndOfTurnButCountersRemain() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new UnbreakableFormation()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownBear.getEffectivePower()).isEqualTo(3);
        assertThat(ownBear.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void spellCopyDuringOwnMainPhaseDoesNotGetAddendum() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        UnbreakableFormation formation = new UnbreakableFormation();
        harness.setHand(player1, List.of(formation, new Expansion()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.castInstant(player1, 0, 0, formation.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ownBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.VIGILANCE)).isFalse();

        harness.passBothPriorities();

        assertThat(ownBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void opponentsMainPhaseDoesNotEnableAddendum() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new UnbreakableFormation()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(ownBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void postcombatMainPhaseStrengthensEveryOwnCreature() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new UnbreakableFormation()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0);

        for (Permanent bear : List.of(firstBear, secondBear)) {
            assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, bear, Keyword.INDESTRUCTIBLE)).isTrue();
            assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isTrue();
        }
    }

    @Test
    void creaturesEnteringAfterResolutionReceiveNoBenefits() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent earlyBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new UnbreakableFormation(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent lateBear = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(earlyBear.getId()))
                .findFirst().orElseThrow();
        assertThat(earlyBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, earlyBear, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, earlyBear, Keyword.VIGILANCE)).isTrue();
        assertThat(lateBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, lateBear, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, lateBear, Keyword.VIGILANCE)).isFalse();
    }
}
