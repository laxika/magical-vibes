package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KimahriValiantGuardian.class, GrizzlyBears.class, ProdigalPyromancer.class,
        Clone.class, Unsummon.class})
class KimahriValiantGuardianTest extends BaseCardTest {

    @Test
    void targetsOnlyCreatureAnOpponentControls() {
        Permanent kimahri = addCreatureReady(player1, new KimahriValiantGuardian());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(opposingBear.getId())
                .doesNotContain(kimahri.getId(), ownBear.getId());
    }

    @Test
    void countersAndTapsBeforeOptionalCopy() {
        Permanent kimahri = addCreatureReady(player1, new KimahriValiantGuardian());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, opposingBear.getId());
        harness.passBothPriorities();

        assertThat(kimahri.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingBear.isTapped()).isTrue();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(kimahri.getCard().getName()).isEqualTo("Kimahri, Valiant Guardian");
        assertThat(gqs.getEffectivePower(gd, kimahri)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, kimahri)).isEqualTo(4);
    }

    @Test
    void acceptedCopyKeepsNameVigilanceAndRonsoRage() {
        Permanent kimahri = addCreatureReady(player1, new KimahriValiantGuardian());
        Permanent firstTarget = addCreatureReady(player2, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, firstTarget.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(kimahri.getCard().getName()).isEqualTo("Kimahri, Valiant Guardian");
        assertThat(gqs.getEffectivePower(gd, kimahri)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, kimahri)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, kimahri, Keyword.VIGILANCE)).isTrue();

        Permanent secondTarget = addCreatureReady(player2, new ProdigalPyromancer());
        advanceToBeginningOfCombat(player1);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(secondTarget.getId());

        harness.handlePermanentChosen(player1, secondTarget.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(kimahri.getCard().getName()).isEqualTo("Kimahri, Valiant Guardian");
        assertThat(kimahri.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, kimahri, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void noOpponentCreatureMeansNoCounter() {
        Permanent kimahri = addCreatureReady(player1, new KimahriValiantGuardian());

        advanceToBeginningOfCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(kimahri.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerOnOpponentsTurn() {
        Permanent kimahri = addCreatureReady(player1, new KimahriValiantGuardian());
        addCreatureReady(player2, new GrizzlyBears());

        advanceToBeginningOfCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(kimahri.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void illegalTargetPreventsCounterAndCopy() {
        Permanent kimahri = addCreatureReady(player1, new KimahriValiantGuardian());
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, bear.getId());

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(kimahri.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, kimahri)).isEqualTo(3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copyingTransformedKimahriCopiesVigilanceAndRonsoRageButNotCounters() {
        Permanent kimahri = addCreatureReady(player1, new KimahriValiantGuardian());
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, kimahri.getId());

        Permanent clone = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(kimahri.getId()))
                .findFirst().orElseThrow();
        assertThat(clone.getCard().getName()).isEqualTo("Kimahri, Valiant Guardian");
        assertThat(gqs.getEffectivePower(gd, clone)).isEqualTo(2);
        assertThat(clone.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, clone, Keyword.VIGILANCE)).isTrue();

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, kimahri.getId());
        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(clone.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
