package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.r.RaiseTheAlarm;
import com.github.laxika.magicalvibes.cards.d.DoubleMajor;
import com.github.laxika.magicalvibes.cards.r.RecklessAmplimancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThalisseReverentMedium.class, RaiseTheAlarm.class, DoubleMajor.class, RecklessAmplimancer.class})
class ThalisseReverentMediumTest extends BaseCardTest {

    @Test
    void createsOneSpiritForEachTokenCreatedThisTurn() {
        harness.addToBattlefield(player1, new ThalisseReverentMedium());
        castRaiseTheAlarm();

        advanceToEndStep();

        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
    }

    @Test
    void createsNoSpiritsWhenNoTokensWereCreatedThisTurn() {
        harness.addToBattlefield(player1, new ThalisseReverentMedium());

        advanceToEndStep();

        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    void countsTokensCreatedBeforeThalisseEntered() {
        castRaiseTheAlarm();
        harness.addToBattlefield(player1, new ThalisseReverentMedium());

        advanceToEndStep();

        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
    }

    @Test
    void countsTokensCreatedInResponseToInitiallyZeroTrigger() {
        harness.addToBattlefield(player1, new ThalisseReverentMedium());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        castRaiseTheAlarm();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
    }

    @Test
    void triggersDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new ThalisseReverentMedium());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        castRaiseTheAlarm();

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
    }

    @Test
    void doesNotCountOpponentsTokens() {
        harness.addToBattlefield(player1, new ThalisseReverentMedium());
        harness.setHand(player2, List.of(new RaiseTheAlarm()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0);

        advanceToEndStep();

        assertThat(findPermanents(player2, "Soldier")).hasSize(2);
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    void doesNotCountTokensFromPreviousTurn() {
        harness.addToBattlefield(player1, new ThalisseReverentMedium());
        castRaiseTheAlarm();
        advanceToEndStep();
        assertThat(findPermanents(player1, "Spirit")).hasSize(2);

        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
    }

    @Test
    void doesNotCountTokenFromCopiedPermanentSpell() {
        harness.addToBattlefield(player1, new ThalisseReverentMedium());
        RecklessAmplimancer creature = new RecklessAmplimancer();
        harness.setHand(player1, List.of(creature, new DoubleMajor()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Reckless Amplimancer")).hasSize(2);

        advanceToEndStep();

        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    private void castRaiseTheAlarm() {
        harness.setHand(player1, List.of(new RaiseTheAlarm()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
