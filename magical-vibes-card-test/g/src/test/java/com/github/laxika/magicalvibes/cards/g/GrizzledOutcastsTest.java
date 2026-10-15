package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.m.Moonmist;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrizzledOutcasts.class, Moonmist.class})
class GrizzledOutcastsTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms to Krallenhorde Wantons when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        harness.addToBattlefield(player1, new GrizzledOutcasts());
        Permanent outcasts = findPermanent(player1, "Grizzled Outcasts");

        gd.spellsCastLastTurn.clear();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve triggered ability

        assertThat(outcasts.isTransformed()).isTrue();
        assertThat(outcasts.getCard().getName()).isEqualTo("Krallenhorde Wantons");
        assertThat(gqs.getEffectivePower(gd, outcasts)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, outcasts)).isEqualTo(7);
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        harness.addToBattlefield(player1, new GrizzledOutcasts());
        Permanent outcasts = findPermanent(player1, "Grizzled Outcasts");

        gd.spellsCastLastTurn.put(player1.getId(), 1);

        advanceToUpkeep(player1);

        assertThat(outcasts.isTransformed()).isFalse();
        assertThat(outcasts.getCard().getName()).isEqualTo("Grizzled Outcasts");
    }

    @Test
    @DisplayName("Krallenhorde Wantons transforms back when a player cast two or more spells last turn")
    void wantonsTransformsBackWhenTwoSpellsCast() {
        harness.addToBattlefield(player1, new GrizzledOutcasts());
        Permanent outcasts = findPermanent(player1, "Grizzled Outcasts");

        // Transform to Krallenhorde Wantons first
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(outcasts.isTransformed()).isTrue();

        // Now simulate that a player cast 2+ spells last turn
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve transform back

        assertThat(outcasts.isTransformed()).isFalse();
        assertThat(outcasts.getCard().getName()).isEqualTo("Grizzled Outcasts");
        assertThat(gqs.getEffectivePower(gd, outcasts)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, outcasts)).isEqualTo(4);
    }

    @Test
    @DisplayName("Krallenhorde Wantons does not transform back when only one spell was cast last turn")
    void wantonsDoesNotTransformWhenOneSpellCast() {
        harness.addToBattlefield(player1, new GrizzledOutcasts());
        Permanent outcasts = findPermanent(player1, "Grizzled Outcasts");

        // Transform to Krallenhorde Wantons first
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(outcasts.isTransformed()).isTrue();

        // Only 1 spell cast last turn by each player
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        advanceToUpkeep(player2);

        assertThat(outcasts.isTransformed()).isTrue();
        assertThat(outcasts.getCard().getName()).isEqualTo("Krallenhorde Wantons");
    }

    @Test
    @DisplayName("Transform triggers on opponent's upkeep too")
    void transformTriggersOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new GrizzledOutcasts());
        Permanent outcasts = findPermanent(player1, "Grizzled Outcasts");

        gd.spellsCastLastTurn.clear();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(outcasts.isTransformed()).isTrue();
        assertThat(outcasts.getCard().getName()).isEqualTo("Krallenhorde Wantons");
    }

    @Test
    @DisplayName("A spell cast by the opponent last turn prevents the front-face trigger")
    void opponentSpellPreventsTransformation() {
        Permanent outcasts = harness.addToBattlefieldAndReturn(player1, new GrizzledOutcasts());
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(outcasts.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("The back face stays transformed when no spells were cast last turn")
    void wantonsStaysTransformedWhenNoSpellsCast() {
        Permanent outcasts = harness.addToBattlefieldAndReturn(player1, new GrizzledOutcasts());
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(outcasts.isTransformed()).isTrue();

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(outcasts.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Moonmist in response prevents the pending ability from transforming the creature again")
    void pendingTriggerDoesNotUndoMoonmistTransformation() {
        Permanent outcasts = harness.addToBattlefieldAndReturn(player1, new GrizzledOutcasts());
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.castFromHand(player1, new Moonmist(), "{1}{G}");
        harness.passBothPriorities();
        assertThat(outcasts.isTransformed()).isTrue();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(outcasts.isTransformed()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Two spells cast during the current upkeep do not transform the back face")
    void currentUpkeepSpellsDoNotCountAsLastTurn() {
        Permanent outcasts = harness.addToBattlefieldAndReturn(player1, new GrizzledOutcasts());
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(outcasts.isTransformed()).isTrue();

        harness.castFromHand(player1, new Moonmist(), "{1}{G}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new Moonmist(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(outcasts.isTransformed()).isTrue();
    }
}
