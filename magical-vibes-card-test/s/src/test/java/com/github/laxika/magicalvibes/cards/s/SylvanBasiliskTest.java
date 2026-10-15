package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GoldenBear;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PincherBeetles;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SylvanBasilisk.class, GoldenBear.class, GrizzlyBears.class, PincherBeetles.class, Unsummon.class})
class SylvanBasiliskTest extends BaseCardTest {

    @Test
    @DisplayName("When Sylvan Basilisk becomes blocked, it creates a non-targeting trigger for that blocker")
    void becomesBlockedCreatesNonTargetingTrigger() {
        Permanent basilisk = addCreatureReady(player1, new SylvanBasilisk());
        basilisk.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GoldenBear());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Sylvan Basilisk");
        assertThat(entry.getTargetId()).isEqualTo(blocker.getId());
        assertThat(entry.getSourcePermanentId()).isEqualTo(basilisk.getId());
        assertThat(entry.isNonTargeting()).isTrue();
    }

    @Test
    @DisplayName("Resolving trigger destroys the creature that blocked Sylvan Basilisk")
    void resolvingTriggerDestroysBlocker() {
        Permanent basilisk = addCreatureReady(player1, new SylvanBasilisk());
        basilisk.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GoldenBear());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(blocker.getId()));
        harness.assertInGraveyard(player2, "Golden Bear");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(basilisk.getId()));
    }

    @Test
    @DisplayName("Sylvan Basilisk creates one trigger per blocking creature")
    void createsOneTriggerPerBlockingCreature() {
        addCreatureReady(player1, new SylvanBasilisk()).setAttacking(true);

        addCreatureReady(player2, new GoldenBear());
        addCreatureReady(player2, new GoldenBear());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        long basiliskTriggerCount = gd.stack.stream()
                .filter(entry -> entry.getCard().getName().equals("Sylvan Basilisk"))
                .count();
        assertThat(basiliskTriggerCount).isEqualTo(2);

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Golden Bear");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(c -> c.getName().equals("Golden Bear"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Sylvan Basilisk does not trigger when it blocks a creature")
    void doesNotTriggerWhenItBlocks() {
        Permanent attacker = addCreatureReady(player1, new GoldenBear());
        attacker.setAttacking(true);
        Permanent basilisk = addCreatureReady(player2, new SylvanBasilisk());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(basilisk);
    }

    @Test
    @DisplayName("Resolving the trigger destroys its blocker even if that permanent is no longer a creature")
    void resolvingTriggerDestroysBlockerAfterItStopsBeingACreature() {
        Permanent basilisk = addCreatureReady(player1, new SylvanBasilisk());
        basilisk.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GoldenBear());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        TestCards.mutableCard(blocker).setType(CardType.ARTIFACT);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(blocker.getId()));
        harness.assertInGraveyard(player2, "Golden Bear");
    }

    @Test
    @DisplayName("The destruction trigger destroys a blocker with shroud before combat damage")
    void destroysBlockerWithShroud() {
        Permanent basilisk = addCreatureReady(player1, new SylvanBasilisk());
        basilisk.setAttacking(true);
        addCreatureReady(player2, new PincherBeetles());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Pincher Beetles");
        harness.assertOnBattlefield(player1, "Sylvan Basilisk");
        assertThat(basilisk.getMarkedDamage()).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The blocker is destroyed even after Sylvan Basilisk leaves the battlefield")
    void destroysBlockerAfterSourceLeaves() {
        Permanent basilisk = addCreatureReady(player1, new SylvanBasilisk());
        basilisk.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.castInstant(player1, 0, basilisk.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Sylvan Basilisk");
        harness.assertNotOnBattlefield(player1, "Sylvan Basilisk");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The destruction trigger does nothing if its blocker has left the battlefield")
    void blockerCanBeReturnedToHandInResponse() {
        addCreatureReady(player1, new SylvanBasilisk()).setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.castInstant(player2, 0, blocker.getId());
        resolveAllTriggers();

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
        harness.assertOnBattlefield(player1, "Sylvan Basilisk");
    }
}
