package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.Mossdog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilkenfistFighter.class, Mossdog.class})
class SilkenfistFighterTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked untaps Silkenfist Fighter")
    void becomingBlockedUntapsIt() {
        Permanent fighter = addCreatureReady(player1, new SilkenfistFighter());
        addCreatureReady(player2, new Mossdog());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(fighter.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(fighter.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Becoming blocked untaps only Silkenfist Fighter")
    void becomingBlockedUntapsOnlyIt() {
        Permanent fighter = addCreatureReady(player1, new SilkenfistFighter());
        Permanent otherCreature = addCreatureReady(player1, new Mossdog());
        otherCreature.tap();
        Permanent blocker = addCreatureReady(player2, new Mossdog());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(fighter.isTapped()).isFalse();
        assertThat(otherCreature.isTapped()).isTrue();
        assertThat(blocker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Multiple blockers trigger Silkenfist Fighter only once")
    void multipleBlockersTriggerOnce() {
        Permanent fighter = addCreatureReady(player1, new SilkenfistFighter());
        addCreatureReady(player2, new Mossdog());
        addCreatureReady(player2, new Mossdog());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(fighter.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An unblocked Silkenfist Fighter does not trigger")
    void unblockedDoesNotTrigger() {
        Permanent fighter = addCreatureReady(player1, new SilkenfistFighter());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(fighter.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untapping after becoming blocked leaves Silkenfist Fighter attacking")
    void untappingDoesNotRemoveItFromCombat() {
        Permanent fighter = addCreatureReady(player1, new SilkenfistFighter());
        addCreatureReady(player2, new Mossdog());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, harness::passBothPriorities);

        assertThat(fighter.isTapped()).isFalse();
        assertThat(fighter.isAttacking()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Blocking with Silkenfist Fighter does not trigger its ability")
    void blockingDoesNotTrigger() {
        addCreatureReady(player1, new Mossdog());
        Permanent fighter = addCreatureReady(player2, new SilkenfistFighter());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(fighter.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An already untapped Fighter still triggers and can untap after being tapped in response")
    void alreadyUntappedFighterStillTriggers() {
        Permanent fighter = addCreatureReady(player1, new SilkenfistFighter());
        fighter.setAttacking(true);
        addCreatureReady(player2, new Mossdog());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        fighter.tap();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, harness::passBothPriorities);

        assertThat(fighter.isTapped()).isFalse();
        assertThat(fighter.isAttacking()).isTrue();
    }
}
