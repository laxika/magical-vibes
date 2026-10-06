package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HanweirWatchkeep.class})
class HanweirWatchkeepTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms to Bane of Hanweir when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        Permanent watchkeep = harness.addToBattlefieldAndReturn(player1, new HanweirWatchkeep());

        // spellsCastLastTurn is empty (no spells cast)
        gd.spellsCastLastTurn.clear();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve triggered ability

        assertThat(watchkeep.isTransformed()).isTrue();
        assertThat(watchkeep.getCard().getName()).isEqualTo("Bane of Hanweir");
        assertThat(gqs.getEffectivePower(gd, watchkeep)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, watchkeep)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        Permanent watchkeep = harness.addToBattlefieldAndReturn(player1, new HanweirWatchkeep());

        // Simulate that a spell was cast last turn
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        advanceToUpkeep(player1);

        assertThat(watchkeep.isTransformed()).isFalse();
        assertThat(watchkeep.getCard().getName()).isEqualTo("Hanweir Watchkeep");
    }

    @Test
    @DisplayName("Bane of Hanweir transforms back when a player cast two or more spells last turn")
    void baneTransformsBackWhenTwoSpellsCast() {
        Permanent watchkeep = harness.addToBattlefieldAndReturn(player1, new HanweirWatchkeep());

        // Transform to Bane of Hanweir first
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve transform
        assertThat(watchkeep.isTransformed()).isTrue();

        // Now simulate that a player cast 2+ spells last turn
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve transform back

        assertThat(watchkeep.isTransformed()).isFalse();
        assertThat(watchkeep.getCard().getName()).isEqualTo("Hanweir Watchkeep");
        assertThat(gqs.getEffectivePower(gd, watchkeep)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, watchkeep)).isEqualTo(5);
    }

    @Test
    @DisplayName("Bane of Hanweir does not transform back when only one spell was cast last turn")
    void baneDoesNotTransformWhenOneSpellCast() {
        Permanent watchkeep = harness.addToBattlefieldAndReturn(player1, new HanweirWatchkeep());

        // Transform to Bane of Hanweir first
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(watchkeep.isTransformed()).isTrue();

        // Only 1 spell cast last turn by each player
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        advanceToUpkeep(player2);

        assertThat(watchkeep.isTransformed()).isTrue();
        assertThat(watchkeep.getCard().getName()).isEqualTo("Bane of Hanweir");
    }

    @Test
    @DisplayName("Transform triggers on opponent's upkeep too")
    void transformTriggersOnOpponentUpkeep() {
        Permanent watchkeep = harness.addToBattlefieldAndReturn(player1, new HanweirWatchkeep());

        // No spells cast last turn
        gd.spellsCastLastTurn.clear();

        // Trigger on opponent's upkeep (not player1's)
        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve

        assertThat(watchkeep.isTransformed()).isTrue();
        assertThat(watchkeep.getCard().getName()).isEqualTo("Bane of Hanweir");
    }

    @Test
    @DisplayName("Defender prevents Hanweir Watchkeep from attacking")
    void defenderCannotAttack() {
        addCreatureReady(player1, new HanweirWatchkeep());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Bane of Hanweir must attack when able")
    void baneMustAttackWhenAble() {
        Permanent watchkeep = addCreatureReady(player1, new HanweirWatchkeep());
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(watchkeep.isTransformed()).isTrue();

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Bane of Hanweir can attack after transforming")
    void baneDealsCombatDamage() {
        addCreatureReady(player1, new HanweirWatchkeep());
        harness.setLife(player2, 20);
        advanceToUpkeep(player1);
        resolveAllTriggers();

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Tapped Bane of Hanweir is not required to attack")
    void tappedBaneDoesNotHaveToAttack() {
        Permanent watchkeep = addCreatureReady(player1, new HanweirWatchkeep());
        advanceToUpkeep(player1);
        resolveAllTriggers();
        watchkeep.tap();
        harness.setLife(player2, 20);

        declareAttackers(List.of());

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Transforming does not remove summoning sickness")
    void summoningSickBaneDoesNotHaveToAttack() {
        Permanent watchkeep = harness.addToBattlefieldAndReturn(player1, new HanweirWatchkeep());
        advanceToUpkeep(player2);
        resolveAllTriggers();
        assertThat(watchkeep.isTransformed()).isTrue();
        assertThat(watchkeep.isSummoningSick()).isTrue();
        harness.setLife(player2, 20);

        declareAttackers(List.of());

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A spell cast by the opponent prevents the front face from transforming")
    void opponentsSpellPreventsTransform() {
        Permanent watchkeep = harness.addToBattlefieldAndReturn(player1, new HanweirWatchkeep());
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(watchkeep.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Bane stays transformed when no spells were cast last turn")
    void baneStaysTransformedWithNoSpells() {
        Permanent watchkeep = harness.addToBattlefieldAndReturn(player1, new HanweirWatchkeep());
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(watchkeep.isTransformed()).isTrue();

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(watchkeep.isTransformed()).isTrue();
    }
}
