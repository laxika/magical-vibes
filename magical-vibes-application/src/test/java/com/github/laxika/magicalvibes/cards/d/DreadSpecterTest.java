package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FemerefScouts;
import com.github.laxika.magicalvibes.cards.f.FetidHorror;
import com.github.laxika.magicalvibes.cards.p.PrismaticLace;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DreadSpecter.class, FemerefScouts.class, FetidHorror.class, PrismaticLace.class})
class DreadSpecterTest extends BaseCardTest {

    @Test
    @DisplayName("When Dread Specter becomes blocked by a nonblack creature, that creature is scheduled for end-of-combat destruction")
    void becomesBlockedByNonblackSchedulesDestruction() {
        Permanent specter = addCreatureReady(player1, new DreadSpecter());
        specter.setAttacking(true);
        Permanent scout = addCreatureReady(player2, new FemerefScouts());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Dread Specter")
                        && se.getTargetId().equals(scout.getId()));

        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(scout.getId()));
    }

    @Test
    @DisplayName("A nonblack blocker survives combat damage but is destroyed at end of combat")
    void nonblackBlockerDestroyedAtEndOfCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent specter = addCreatureReady(player1, new DreadSpecter());
        specter.setAttacking(true);
        addCreatureReady(player2, new FemerefScouts()); // 1/4 survives Dread Specter's 2 damage

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Femeref Scouts");
        harness.assertInGraveyard(player2, "Femeref Scouts");
    }

    @Test
    @DisplayName("When Dread Specter becomes blocked by a black creature, nothing is scheduled for destruction")
    void becomesBlockedByBlackSchedulesNothing() {
        Permanent specter = addCreatureReady(player1, new DreadSpecter());
        specter.setAttacking(true);
        addCreatureReady(player2, new FetidHorror());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).noneMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Dread Specter"));

        harness.passBothPriorities();
        assertThat(gd.hasDelayedAction(DelayedPermanentAction.class)).isFalse();
    }

    @Test
    @DisplayName("A nonblack creature that becomes black after blocking is still destroyed at end of combat")
    void nonblackCreatureThatBecomesBlackAfterBlockingIsStillDestroyed() {
        Permanent specter = addCreatureReady(player1, new DreadSpecter());
        specter.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new FemerefScouts());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.setHand(player1, List.of(new PrismaticLace()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, blocker.getId());
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "DONE");

        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(blocker.getId()));
    }

    @Test
    @DisplayName("When Dread Specter blocks a nonblack creature, that attacker is scheduled for end-of-combat destruction")
    void blocksNonblackSchedulesDestruction() {
        Permanent attacker = addCreatureReady(player1, new FemerefScouts());
        attacker.setAttacking(true);
        addCreatureReady(player2, new DreadSpecter());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Dread Specter")
                        && se.getTargetId().equals(attacker.getId()));

        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(attacker.getId()));
    }

    @Test
    @DisplayName("When Dread Specter blocks a black creature, nothing is scheduled for destruction")
    void blocksBlackSchedulesNothing() {
        Permanent attacker = addCreatureReady(player1, new FetidHorror());
        attacker.setAttacking(true);
        addCreatureReady(player2, new DreadSpecter());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).noneMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Dread Specter"));

        harness.passBothPriorities();
        assertThat(gd.hasDelayedAction(DelayedPermanentAction.class)).isFalse();
    }
}
