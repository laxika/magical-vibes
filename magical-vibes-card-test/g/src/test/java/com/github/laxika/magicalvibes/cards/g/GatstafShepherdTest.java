package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.o.OneEyedScarecrow;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GatstafShepherd.class, WalkingCorpse.class, OneEyedScarecrow.class})
class GatstafShepherdTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms to Gatstaf Howler when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        Permanent shepherd = harness.addToBattlefieldAndReturn(player1, new GatstafShepherd());

        // spellsCastLastTurn is empty (no spells cast)
        gd.spellsCastLastTurn.clear();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve triggered ability

        assertThat(shepherd.isTransformed()).isTrue();
        assertThat(shepherd.getCard().getName()).isEqualTo("Gatstaf Howler");
        assertThat(gqs.getEffectivePower(gd, shepherd)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, shepherd)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        Permanent shepherd = harness.addToBattlefieldAndReturn(player1, new GatstafShepherd());

        // Simulate that a spell was cast last turn
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        advanceToUpkeep(player1);

        assertThat(shepherd.isTransformed()).isFalse();
        assertThat(shepherd.getCard().getName()).isEqualTo("Gatstaf Shepherd");
    }

    @Test
    @DisplayName("Gatstaf Howler transforms back when a player cast two or more spells last turn")
    void howlerTransformsBackWhenTwoSpellsCast() {
        Permanent shepherd = harness.addToBattlefieldAndReturn(player1, new GatstafShepherd());

        // Transform to Gatstaf Howler first
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve transform
        assertThat(shepherd.isTransformed()).isTrue();

        // Now simulate that a player cast 2+ spells last turn
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve transform back

        assertThat(shepherd.isTransformed()).isFalse();
        assertThat(shepherd.getCard().getName()).isEqualTo("Gatstaf Shepherd");
        assertThat(gqs.getEffectivePower(gd, shepherd)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, shepherd)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gatstaf Howler does not transform back when only one spell was cast last turn")
    void howlerDoesNotTransformWhenOneSpellCast() {
        Permanent shepherd = harness.addToBattlefieldAndReturn(player1, new GatstafShepherd());

        // Transform to Gatstaf Howler first
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(shepherd.isTransformed()).isTrue();

        // Only 1 spell cast last turn by each player
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        advanceToUpkeep(player2);

        assertThat(shepherd.isTransformed()).isTrue();
        assertThat(shepherd.getCard().getName()).isEqualTo("Gatstaf Howler");
    }

    @Test
    @DisplayName("Transform triggers on opponent's upkeep too")
    void transformTriggersOnOpponentUpkeep() {
        Permanent shepherd = harness.addToBattlefieldAndReturn(player1, new GatstafShepherd());

        // No spells cast last turn
        gd.spellsCastLastTurn.clear();

        // Trigger on opponent's upkeep (not player1's)
        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve

        assertThat(shepherd.isTransformed()).isTrue();
        assertThat(shepherd.getCard().getName()).isEqualTo("Gatstaf Howler");
    }


    @Test
    @DisplayName("A spell cast by the opponent prevents the front-face trigger")
    void opponentSpellPreventsTransformation() {
        Permanent shepherd = harness.addToBattlefieldAndReturn(player1, new GatstafShepherd());
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(shepherd.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Howler stays transformed after a turn with no spells")
    void howlerStaysTransformedWithNoSpells() {
        Permanent shepherd = transformShepherd();

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(shepherd.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Howler cannot be blocked by a nonartifact creature of another color")
    void intimidateRejectsBlackNonartifactBlocker() {
        Permanent howler = transformShepherd();
        Permanent blocker = addCreatureReady(player2, new WalkingCorpse());
        addCreatureReady(player2, new GatstafShepherd());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("intimidate");
        assertThat(howler.isAttacking()).isTrue();
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Howler can be blocked by a green creature")
    void intimidateAllowsGreenBlocker() {
        transformShepherd();
        Permanent blocker = addCreatureReady(player2, new GatstafShepherd());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Howler can be blocked by a colorless artifact creature")
    void intimidateAllowsArtifactBlocker() {
        transformShepherd();
        Permanent blocker = addCreatureReady(player2, new OneEyedScarecrow());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The front face does not have intimidate")
    void shepherdCanBeBlockedByBlackCreature() {
        addCreatureReady(player1, new GatstafShepherd());
        Permanent blocker = addCreatureReady(player2, new WalkingCorpse());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent transformShepherd() {
        Permanent shepherd = addCreatureReady(player1, new GatstafShepherd());
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(shepherd.isTransformed()).isTrue();
        return shepherd;
    }
}
