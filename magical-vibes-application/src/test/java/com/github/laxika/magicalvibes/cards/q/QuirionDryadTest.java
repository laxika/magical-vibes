package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.m.MaggotCarrier;
import com.github.laxika.magicalvibes.cards.m.MoggSentry;
import com.github.laxika.magicalvibes.cards.s.SamitePilgrim;
import com.github.laxika.magicalvibes.cards.s.StarCompass;
import com.github.laxika.magicalvibes.cards.s.StormscapeFamiliar;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.cards.t.ThornscapeFamiliar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        QuirionDryad.class,
        SamitePilgrim.class,
        ThornscapeFamiliar.class,
        StormscapeFamiliar.class,
        MaggotCarrier.class,
        MoggSentry.class,
        StarCompass.class,
        Terminate.class
})
class QuirionDryadTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a white spell makes Quirion Dryad get a +1/+1 counter")
    void ownWhiteSpellAddsCounter() {
        harness.addToBattlefield(player1, new QuirionDryad());
        harness.castFromHand(player1, new SamitePilgrim(), "{1}{W}");

        Permanent dryad = getDryad();
        assertThat(dryad.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        assertThat(dryadTriggersOnStack()).isEqualTo(1);

        harness.passBothPriorities(); // resolve dryad trigger

        assertThat(dryad.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(harness.getGameQueryService().getEffectivePower(gd, dryad)).isEqualTo(2);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, dryad)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a green spell does not trigger Quirion Dryad")
    void ownGreenSpellDoesNotAddCounter() {
        harness.addToBattlefield(player1, new QuirionDryad());
        harness.castFromHand(player1, new ThornscapeFamiliar(), "{1}{G}");

        Permanent dryad = getDryad();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(dryadTriggersOnStack()).isZero();
        assertThat(dryad.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Opponent casting a white spell does not trigger Quirion Dryad")
    void opponentWhiteSpellDoesNotAddCounter() {
        harness.addToBattlefield(player1, new QuirionDryad());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new SamitePilgrim(), "{1}{W}");

        Permanent dryad = getDryad();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(dryadTriggersOnStack()).isZero();
        assertThat(dryad.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Casting blue, black, and red spells makes Quirion Dryad get a counter for each")
    void ownBlueBlackAndRedSpellsAddCounters() {
        harness.addToBattlefield(player1, new QuirionDryad());

        harness.castFromHand(player1, new StormscapeFamiliar(), "{1}{U}");
        resolveAllTriggers();
        harness.castFromHand(player1, new MaggotCarrier(), "{B}");
        resolveAllTriggers();
        harness.castFromHand(player1, new MoggSentry(), "{R}");
        resolveAllTriggers();

        Permanent dryad = getDryad();
        assertThat(dryad.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting a colorless spell does not trigger Quirion Dryad")
    void ownColorlessSpellDoesNotAddCounter() {
        harness.addToBattlefield(player1, new QuirionDryad());
        harness.castFromHand(player1, new StarCompass(), "{2}");

        Permanent dryad = getDryad();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(dryadTriggersOnStack()).isZero();
        assertThat(dryad.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Casting a black-red instant triggers Quirion Dryad")
    void ownMulticoloredSpellAddsCounter() {
        harness.addToBattlefield(player1, new QuirionDryad());
        Permanent target = addCreatureReady(player2, new ThornscapeFamiliar());

        harness.setHand(player1, List.of(new Terminate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());

        assertThat(dryadTriggersOnStack()).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(getDryad().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent getDryad() {
        return findPermanent(player1, "Quirion Dryad");
    }

    private long dryadTriggersOnStack() {
        return gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Quirion Dryad"))
                .count();
    }
}
