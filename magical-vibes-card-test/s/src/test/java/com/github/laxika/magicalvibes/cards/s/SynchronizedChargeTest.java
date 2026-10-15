package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SynchronizedCharge.class, GrizzlyBears.class, HillGiant.class})
class SynchronizedChargeTest extends BaseCardTest {

    @Test
    @DisplayName("Distributes counters and grants both keywords to creatures with counters")
    void distributesCountersAndGrantsKeywordsToCounteredCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent alreadyCountered = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        alreadyCountered.setCounterCount(CounterType.LOYALTY, 1);
        Permanent withoutCounters = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SynchronizedCharge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(first.getId(), second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(first.hasKeyword(Keyword.VIGILANCE)).isTrue();
        assertThat(first.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(second.hasKeyword(Keyword.VIGILANCE)).isTrue();
        assertThat(second.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(alreadyCountered.hasKeyword(Keyword.VIGILANCE)).isTrue();
        assertThat(alreadyCountered.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(withoutCounters.hasKeyword(Keyword.VIGILANCE)).isFalse();
        assertThat(withoutCounters.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(opponentCreature.hasKeyword(Keyword.VIGILANCE)).isFalse();
        assertThat(opponentCreature.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Counters and keywords wear off at end of turn only for the keyword grant")
    void keywordsWearOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SynchronizedCharge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(creature.getId()));
        assertThat(creature.hasKeyword(Keyword.VIGILANCE)).isTrue();
        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.hasKeyword(Keyword.VIGILANCE)).isFalse();
        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SynchronizedCharge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Harmonize taps a creature to reduce the cost and exiles the spell")
    void harmonizeTapsCreatureAndExilesSpell() {
        Card spell = new SynchronizedCharge();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0, List.of(creature.getId()), List.of(creature.getId()));

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Synchronized Charge"));
    }

    @Test
    void survivingTargetReceivesOnlyItsAssignedCounter() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new SynchronizedCharge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(second);
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, first, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, first, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void allIllegalTargetsPreventTheUntargetedKeywordGrant() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        other.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new SynchronizedCharge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, List.of(target.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, other, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.TRAMPLE)).isFalse();
        harness.assertInGraveyard(player1, "Synchronized Charge");
    }

    @Test
    void keywordRecipientsAreFixedAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new SynchronizedCharge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId()));
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        other.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent lateCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        lateCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, lateCreature, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, lateCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void harmonizeCanBePaidWithoutTappingACreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new SynchronizedCharge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFlashback(player1, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Synchronized Charge"));
        harness.assertNotInGraveyard(player1, "Synchronized Charge");
    }

    @Test
    void harmonizeReductionUsesPowerBeforeTheSpellAddsCounters() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setGraveyard(player1, List.of(new SynchronizedCharge()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castFlashback(player1, 0, List.of(creature.getId()), List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void harmonizeStillRequiresGreenManaEvenWithExcessPower() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setGraveyard(player1, List.of(new SynchronizedCharge()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0,
                List.of(creature.getId()), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Synchronized Charge");
    }

    @Test
    void harmonizeExilesTheSpellWhenAllTargetsBecomeIllegal() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setGraveyard(player1, List.of(new SynchronizedCharge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0, List.of(creature.getId()), List.of(creature.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Synchronized Charge"));
        harness.assertNotInGraveyard(player1, "Synchronized Charge");
    }

    @Test
    void harmonizeCannotTapAnAlreadyTappedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        creature.tap();
        harness.setGraveyard(player1, List.of(new SynchronizedCharge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0,
                List.of(creature.getId()), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Synchronized Charge");
    }
}
