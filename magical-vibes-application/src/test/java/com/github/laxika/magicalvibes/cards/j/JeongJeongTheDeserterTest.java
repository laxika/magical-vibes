package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.FirebendingLesson;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JeongJeongTheDeserter.class, FirebendingLesson.class, HillGiant.class, Shock.class})
class JeongJeongTheDeserterTest extends BaseCardTest {

    @Test
    void firebendingAddsRedManaUntilEndOfCombat() {
        addCreatureReady(player1, new JeongJeongTheDeserter());

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void exhaustCountersAndCopiesTheNextLessonButNotOtherSpells() {
        addCreatureReady(player1, new JeongJeongTheDeserter());
        Permanent target = addCreatureReady(player2, new HillGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst()
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isZero();

        harness.setHand(player1, List.of(new FirebendingLesson()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void lessonCopyCanChooseANewTargetAndOnlyTheNextLessonIsCopied() {
        Permanent jeongJeong = addCreatureReady(player1, new JeongJeongTheDeserter());
        Permanent opposingJeongJeong = addCreatureReady(player2, new JeongJeongTheDeserter());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new FirebendingLesson(), new FirebendingLesson()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, opposingJeongJeong.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, jeongJeong.getId());
        resolveAllTriggers();

        assertThat(jeongJeong.getMarkedDamage()).isEqualTo(2);
        assertThat(opposingJeongJeong.getMarkedDamage()).isEqualTo(2);

        harness.castAndResolveInstant(player1, 0, jeongJeong.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(jeongJeong.getMarkedDamage()).isEqualTo(4);
        harness.assertNotOnBattlefield(player1, "Jeong Jeong, the Deserter");
        harness.assertInGraveyard(player1, "Jeong Jeong, the Deserter");
    }

    @Test
    void opponentsLessonDoesNotConsumeTheDelayedCopy() {
        Permanent jeongJeong = addCreatureReady(player1, new JeongJeongTheDeserter());
        Permanent opposingJeongJeong = addCreatureReady(player2, new JeongJeongTheDeserter());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new FirebendingLesson()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, jeongJeong.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(jeongJeong.getMarkedDamage()).isEqualTo(2);

        harness.setHand(player1, List.of(new FirebendingLesson()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, opposingJeongJeong.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Jeong Jeong, the Deserter");
        harness.assertInGraveyard(player2, "Jeong Jeong, the Deserter");
    }

    @Test
    void delayedCopyExpiresAtTheEndOfTheTurn() {
        addCreatureReady(player1, new JeongJeongTheDeserter());
        Permanent target = addCreatureReady(player2, new JeongJeongTheDeserter());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setLibrary(player2, List.of(new FirebendingLesson()));
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player1, List.of(new FirebendingLesson()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Jeong Jeong, the Deserter");
    }

    @Test
    void exhaustActivationCannotBeRepeatedWhileItIsStillOnTheStack() {
        addCreatureReady(player1, new JeongJeongTheDeserter());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");

        resolveAllTriggers();
    }

    @Test
    void exhaustAbilityCanBeActivatedOnlyOnce() {
        addCreatureReady(player1, new JeongJeongTheDeserter());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }
}
