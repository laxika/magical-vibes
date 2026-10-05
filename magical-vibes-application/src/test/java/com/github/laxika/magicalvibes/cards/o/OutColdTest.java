package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OutCold.class, Cancel.class, Forest.class, GiantSpider.class, GrizzlyBears.class, Unsummon.class})
class OutColdTest extends BaseCardTest {

    @Test
    @DisplayName("Taps and stuns up to two target creatures and investigates")
    void tapsAndStunsTwoCreaturesAndInvestigates() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new OutCold()));
        addMana();

        harness.castAndResolveInstant(player1, 0, List.of(bear.getId(), spider.getId()));

        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(spider.isTapped()).isTrue();
        assertThat(spider.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Investigates when no creatures are targeted")
    void investigatesWithNoCreatureTargets() {
        harness.setHand(player1, List.of(new OutCold()));
        addMana();

        harness.castAndResolveInstant(player1, 0, List.of());

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new OutCold()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be countered")
    void cannotBeCountered() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        OutCold outCold = new OutCold();
        harness.setHand(player1, List.of(outCold));
        addMana();
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, bear.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, outCold.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Cancel");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void stunsAnAlreadyTappedFriendlyCreatureAndPreventsItsNextUntap() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.tap();
        harness.setHand(player1, List.of(new OutCold()));
        addMana();

        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(bear.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        harness.performUntapStep(player1);
        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getCounterCount(CounterType.STUN)).isZero();
        harness.performUntapStep(player1);
        assertThat(bear.isTapped()).isFalse();
    }

    @Test
    void resolvesForTheRemainingLegalTargetAndInvestigates() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new OutCold()));
        harness.setHand(player2, List.of(new Unsummon()));
        addMana();
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, List.of(bear.getId(), spider.getId()));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bear.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(spider.isTapped()).isTrue();
        assertThat(spider.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void doesNotInvestigateWhenItsOnlyTargetBecomesIllegal() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OutCold()));
        harness.setHand(player2, List.of(new Unsummon()));
        addMana();
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, bear.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bear.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Out Cold");
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
