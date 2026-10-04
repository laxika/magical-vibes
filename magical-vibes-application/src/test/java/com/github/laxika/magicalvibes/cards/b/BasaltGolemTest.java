package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FeralShadow;
import com.github.laxika.magicalvibes.cards.g.GiantMantis;
import com.github.laxika.magicalvibes.cards.i.ImprisonedInTheMoon;
import com.github.laxika.magicalvibes.cards.p.PatagiaGolem;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.SacrificeAtEndOfCombat;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BasaltGolem.class, PatagiaGolem.class, GiantMantis.class, FeralShadow.class,
        ImprisonedInTheMoon.class})
class BasaltGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Basalt Golem can't be blocked by an artifact creature")
    void cannotBeBlockedByArtifactCreature() {
        Permanent golem = addCreatureReady(player1, new BasaltGolem());
        golem.setAttacking(true);
        Permanent artifactBlocker = addCreatureReady(player2, new PatagiaGolem());

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(artifactBlocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(golem);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Basalt Golem can be blocked by a nonartifact creature")
    void canBeBlockedByNonartifactCreature() {
        Permanent golem = addCreatureReady(player1, new BasaltGolem());
        golem.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GiantMantis());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Becoming blocked schedules the blocker for sacrifice at end of combat")
    void becomesBlockedSchedulesSacrifice() {
        Permanent golem = addCreatureReady(player1, new BasaltGolem());
        golem.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GiantMantis());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Basalt Golem")
                        && blocker.getId().equals(se.getTargetId()));

        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(SacrificeAtEndOfCombat.class))
                .anyMatch(a -> a.permanentId().equals(blocker.getId()));
    }

    @Test
    @DisplayName("The blocker survives combat damage, is sacrificed at end of combat, and its controller gets a Wall token")
    void blockerSacrificedAtEndOfCombatAndControllerGetsWall() {
        Permanent golem = addCreatureReady(player1, new BasaltGolem());
        golem.setAttacking(true);
        addCreatureReady(player2, new GiantMantis());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Giant Mantis");
        harness.assertInGraveyard(player2, "Giant Mantis");

        Permanent wall = findPermanent(player2, "Wall");
        assertThat(wall.getCard().getPower()).isZero();
        assertThat(wall.getCard().getToughness()).isEqualTo(2);
        assertThat(gqs.isArtifact(gd, wall)).isTrue();
        assertThat(gqs.isCreature(gd, wall)).isTrue();
        assertThat(wall.getCard().getSubtypes()).contains(CardSubtype.WALL);
        assertThat(gqs.hasKeyword(gd, wall, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, wall)).isEmpty();
    }

    @Test
    @DisplayName("No Wall token is created when the blocker died to combat damage before end of combat")
    void noWallTokenWhenBlockerAlreadyDied() {
        Permanent golem = addCreatureReady(player1, new BasaltGolem());
        golem.setAttacking(true);
        addCreatureReady(player2, new FeralShadow());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Feral Shadow");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Wall"));
    }

    @Test
    @DisplayName("Creates one Wall token for each blocker sacrificed at end of combat")
    void createsOneWallForEachSacrificedBlocker() {
        Permanent golem = addCreatureReady(player1, new BasaltGolem());
        golem.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new GiantMantis());
        addCreatureReady(player2, new GiantMantis());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        resolveAllTriggers();
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(firstBlocker.getId(), 2));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Wall")).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Giant Mantis"))
                .hasSize(2);
    }

    @Test
    @CardUsed(ImprisonedInTheMoon.class)
    @DisplayName("Still schedules the blocker if it becomes a noncreature before the trigger resolves")
    void schedulesSacrificeWhenBlockerBecomesNoncreatureBeforeTriggerResolves() {
        Permanent golem = addCreatureReady(player1, new BasaltGolem());
        golem.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GiantMantis());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new ImprisonedInTheMoon());
        aura.setAttachedTo(blocker.getId());

        assertThat(gqs.isCreature(gd, blocker)).isFalse();
        assertThat(gqs.isLand(gd, blocker)).isTrue();

        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(SacrificeAtEndOfCombat.class))
                .anyMatch(a -> a.permanentId().equals(blocker.getId()));
    }

    @Test
    @DisplayName("End-of-combat sacrifice uses the stack and leaves a response window before creating the Wall")
    void delayedSacrificeWaitsForResolutionAtEndOfCombat() {
        Permanent golem = addCreatureReady(player1, new BasaltGolem());
        golem.setAttacking(true);
        addCreatureReady(player2, new GiantMantis());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(player1, TurnStep.END_OF_COMBAT);

        harness.assertOnBattlefield(player2, "Giant Mantis");
        harness.assertNotInGraveyard(player2, "Giant Mantis");
        assertThat(findPermanents(player2, "Wall")).isEmpty();
        assertThat(gd.stack).anyMatch(entry ->
                entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && entry.getCard().getName().equals("Basalt Golem")
                        && entry.getControllerId().equals(player1.getId()));

        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Giant Mantis");
        assertThat(findPermanents(player2, "Wall")).hasSize(1);
    }

    @Test
    @DisplayName("An unblocked Basalt Golem creates no Wall and schedules no sacrifice")
    void unblockedGolemDoesNotCreateWall() {
        Permanent golem = addCreatureReady(player1, new BasaltGolem());
        golem.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        harness.assertLife(player2, 18);
        assertThat(findPermanents(player1, "Wall")).isEmpty();
        assertThat(findPermanents(player2, "Wall")).isEmpty();
        assertThat(gd.getDelayedActions(SacrificeAtEndOfCombat.class)).isEmpty();
    }
}
