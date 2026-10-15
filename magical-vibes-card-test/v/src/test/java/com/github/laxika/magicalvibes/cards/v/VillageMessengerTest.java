package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VillageMessenger.class})
class VillageMessengerTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms to Moonrise Intruder when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        Permanent messenger = harness.addToBattlefieldAndReturn(player1, new VillageMessenger());

        gd.spellsCastLastTurn.clear();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve triggered ability

        assertThat(messenger.isTransformed()).isTrue();
        assertThat(messenger.getCard().getName()).isEqualTo("Moonrise Intruder");
        assertThat(gqs.getEffectivePower(gd, messenger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, messenger)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        Permanent messenger = harness.addToBattlefieldAndReturn(player1, new VillageMessenger());

        gd.spellsCastLastTurn.put(player1.getId(), 1);

        advanceToUpkeep(player1);

        assertThat(messenger.isTransformed()).isFalse();
        assertThat(messenger.getCard().getName()).isEqualTo("Village Messenger");
    }

    @Test
    @DisplayName("Moonrise Intruder transforms back when a player cast two or more spells last turn")
    void intruderTransformsBackWhenTwoSpellsCast() {
        Permanent messenger = harness.addToBattlefieldAndReturn(player1, new VillageMessenger());

        // Transform to Moonrise Intruder first
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(messenger.isTransformed()).isTrue();

        // Now simulate that a player cast 2+ spells last turn
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve transform back

        assertThat(messenger.isTransformed()).isFalse();
        assertThat(messenger.getCard().getName()).isEqualTo("Village Messenger");
        assertThat(gqs.getEffectivePower(gd, messenger)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, messenger)).isEqualTo(1);
    }

    @Test
    @DisplayName("Moonrise Intruder does not transform back when only one spell was cast last turn")
    void intruderDoesNotTransformWhenOneSpellCast() {
        Permanent messenger = harness.addToBattlefieldAndReturn(player1, new VillageMessenger());

        // Transform to Moonrise Intruder first
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(messenger.isTransformed()).isTrue();

        // Only 1 spell cast last turn by each player
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        advanceToUpkeep(player2);

        assertThat(messenger.isTransformed()).isTrue();
        assertThat(messenger.getCard().getName()).isEqualTo("Moonrise Intruder");
    }

    @Test
    @DisplayName("Transform triggers on opponent's upkeep too")
    void transformTriggersOnOpponentUpkeep() {
        Permanent messenger = harness.addToBattlefieldAndReturn(player1, new VillageMessenger());

        gd.spellsCastLastTurn.clear();

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve

        assertThat(messenger.isTransformed()).isTrue();
        assertThat(messenger.getCard().getName()).isEqualTo("Moonrise Intruder");
    }

    @Test
    void opponentsSpellPreventsTransformation() {
        Permanent messenger = harness.addToBattlefieldAndReturn(player1, new VillageMessenger());
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(messenger.isTransformed()).isFalse();
    }

    @Test
    void backFaceRemainsTransformedAfterSpelllessTurn() {
        Permanent messenger = harness.addToBattlefieldAndReturn(player1, new VillageMessenger());
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(messenger.isTransformed()).isTrue();

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(messenger.isTransformed()).isTrue();
    }

    @Test
    void hasteAllowsAttackingImmediately() {
        Permanent messenger = harness.addToBattlefieldAndReturn(player1, new VillageMessenger());
        assertThat(messenger.isSummoningSick()).isTrue();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(messenger.isAttacking()).isTrue();
    }

    @Test
    void transformedCreatureRequiresTwoBlockers() {
        Permanent messenger = harness.addToBattlefieldAndReturn(player1, new VillageMessenger());
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(messenger.isTransformed()).isTrue();
        messenger.setSummoningSick(false);
        addCreatureReady(player2, new VillageMessenger());
        addCreatureReady(player2, new VillageMessenger());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked except by two or more creatures");
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
    }
}
