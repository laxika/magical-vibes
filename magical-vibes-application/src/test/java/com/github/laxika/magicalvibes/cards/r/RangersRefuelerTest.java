package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KeenBuccaneer;
import com.github.laxika.magicalvibes.cards.t.TezzeretAgentOfBolas;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Rangers' Refueler")
@CardUsed({RangersRefueler.class, Forest.class, KeenBuccaneer.class, TezzeretAgentOfBolas.class})
class RangersRefuelerTest extends BaseCardTest {

    @Test
    @DisplayName("Exhaust draws before the ability resolves, animates permanently, and adds a counter")
    void exhaustDrawsBeforeAbilityAndAnimatesPermanently() {
        Permanent refueler = addReadyRefueler();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gqs.isCreature(gd, refueler)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, refueler)).isTrue();
        assertThat(refueler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, refueler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, refueler)).isEqualTo(4);
    }

    @Test
    @DisplayName("Crew does not trigger the exhaust draw")
    void crewDoesNotTriggerExhaustDraw() {
        addReadyRefueler();
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new KeenBuccaneer());
        crew.setSummoningSick(false);
        harness.setLibrary(player1, List.of(new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("The exhaust ability can be activated only once")
    void exhaustCanBeActivatedOnlyOnce() {
        addReadyRefueler();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    @DisplayName("Exhaust preserves base power and toughness set by another effect")
    void exhaustPreservesExistingBasePowerAndToughness() {
        Permanent refueler = addReadyRefueler();
        Permanent tezzeret = harness.addToBattlefieldAndReturn(player1, new TezzeretAgentOfBolas());
        tezzeret.setCounterCount(CounterType.LOYALTY, 3);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 1, 1, null, refueler.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, refueler)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, refueler)).isEqualTo(5);

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(refueler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, refueler)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, refueler)).isEqualTo(6);
    }

    @Test
    @DisplayName("Exhaust animation and its counter survive turn cleanup")
    void exhaustAnimationSurvivesCleanup() {
        Permanent refueler = addReadyRefueler();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, refueler)).isTrue();
        assertThat(gqs.isArtifact(gd, refueler)).isTrue();
        assertThat(refueler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, refueler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, refueler)).isEqualTo(4);
    }

    @Test
    @DisplayName("Each controlled Refueler draws for another permanent's exhaust activation")
    void eachRefuelerTriggersForAnotherPermanent() {
        Permanent first = addReadyRefueler();
        Permanent second = addReadyRefueler();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gqs.isCreature(gd, second)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, second)).isTrue();
        assertThat(gqs.isCreature(gd, first)).isFalse();
    }

    @Test
    @DisplayName("An opponent's exhaust activation does not trigger your Refueler")
    void opponentsExhaustDoesNotTriggerDraw() {
        addReadyRefueler();
        harness.addToBattlefield(player2, new RangersRefueler());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        int ownHandBefore = gd.playerHands.get(player1.getId()).size();
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(ownHandBefore);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 1);
        harness.passBothPriorities();
    }

    private Permanent addReadyRefueler() {
        Permanent refueler = harness.addToBattlefieldAndReturn(player1, new RangersRefueler());
        refueler.setSummoningSick(false);
        return refueler;
    }
}
