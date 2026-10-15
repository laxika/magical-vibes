package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GatherTheTownsfolk;
import com.github.laxika.magicalvibes.cards.s.SanctuaryCat;
import com.github.laxika.magicalvibes.cards.h.HeavyMattock;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MidnightGuard.class, SanctuaryCat.class, HeavyMattock.class, GatherTheTownsfolk.class})
class MidnightGuardTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps when controller's other creature enters")
    void untapsWhenControllerCreatureEnters() {
        Permanent guard = addCreatureReady(player1, new MidnightGuard());
        guard.tap();

        harness.castFromHand(player1, new SanctuaryCat(), "{W}");

        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Midnight Guard");

        harness.passBothPriorities(); // resolve Midnight Guard trigger

        assertThat(guard.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untaps when opponent's creature enters")
    void untapsWhenOpponentCreatureEnters() {
        Permanent guard = addCreatureReady(player1, new MidnightGuard());
        guard.tap();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new SanctuaryCat(), "{W}");

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve Midnight Guard trigger

        assertThat(guard.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not trigger when Midnight Guard itself enters")
    void doesNotTriggerForSelfEntering() {
        harness.castFromHand(player1, new MidnightGuard(), "{2}{W}");

        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when a noncreature permanent enters")
    void doesNotTriggerForNonCreaturePermanent() {
        Permanent guard = addCreatureReady(player1, new MidnightGuard());
        guard.tap();

        harness.castFromHand(player1, new HeavyMattock(), "{3}");

        harness.passBothPriorities(); // resolve artifact spell

        assertThat(gd.stack).isEmpty();
        assertThat(guard.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Triggers while untapped and untaps only when the ability resolves")
    void triggersWhileUntapped() {
        Permanent guard = addCreatureReady(player1, new MidnightGuard());

        harness.castFromHand(player1, new SanctuaryCat(), "{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        guard.tap();
        assertThat(guard.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(guard.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Another Midnight Guard entering untaps the existing Guard only")
    void anotherGuardTriggersExistingGuard() {
        Permanent guard = addCreatureReady(player1, new MidnightGuard());
        guard.tap();

        harness.castFromHand(player1, new MidnightGuard(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(guard.isTapped()).isTrue();
        Permanent enteringGuard = findPermanents(player1, "Midnight Guard").get(1);
        enteringGuard.tap();

        harness.passBothPriorities();

        assertThat(guard.isTapped()).isFalse();
        assertThat(enteringGuard.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps even if the entering creature leaves before the trigger resolves")
    void enteringCreatureLeavingDoesNotStopUntap() {
        Permanent guard = addCreatureReady(player1, new MidnightGuard());
        guard.tap();

        harness.castFromHand(player1, new SanctuaryCat(), "{W}");
        harness.passBothPriorities();

        Permanent cat = findPermanent(player1, "Sanctuary Cat");
        gd.playerBattlefields.get(player1.getId()).remove(cat);
        gd.playerGraveyards.get(player1.getId()).add(cat.getCard());

        harness.passBothPriorities();

        assertThat(guard.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Each creature token entering creates a separate untap trigger")
    void triggersForEachCreatureToken() {
        Permanent guard = addCreatureReady(player1, new MidnightGuard());
        guard.tap();

        harness.castFromHand(player1, new GatherTheTownsfolk(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        assertThat(guard.isTapped()).isTrue();

        harness.passBothPriorities();
        assertThat(guard.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);

        guard.tap();
        harness.passBothPriorities();

        assertThat(guard.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A departed Guard's trigger does not untap another Guard")
    void departedSourceDoesNotUntapAnotherGuard() {
        Permanent guard = addCreatureReady(player1, new MidnightGuard());
        guard.tap();

        harness.castFromHand(player1, new SanctuaryCat(), "{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(guard);
        gd.playerGraveyards.get(player1.getId()).add(guard.getCard());
        Permanent replacementGuard = addCreatureReady(player1, new MidnightGuard());
        replacementGuard.tap();

        harness.passBothPriorities();

        assertThat(replacementGuard.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
