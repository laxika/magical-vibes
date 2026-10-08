package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CathedralSanctifier;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SomberwaldVigilante.class, GrizzlyBears.class, CathedralSanctifier.class})
class SomberwaldVigilanteTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked deals 1 damage to the blocker")
    void becomingBlockedDeals1DamageToBlocker() {
        Permanent vigilante = addCreatureReady(player1, new SomberwaldVigilante());
        vigilante.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(blocker.getId());
        assertThat(entry.getSourcePermanentId()).isEqualTo(vigilante.getId());
        assertThat(entry.isNonTargeting()).isTrue();

        harness.passBothPriorities();

        Permanent damagedBlocker = findPermanent(player2, "Grizzly Bears");
        assertThat(damagedBlocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Becoming blocked by two creatures fires once per blocker")
    void becomingBlockedByTwoCreaturesFiresPerBlocker() {
        Permanent vigilante = addCreatureReady(player1, new SomberwaldVigilante());
        vigilante.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gd.stack).hasSize(2);

        resolveAllTriggers();

        List<Permanent> bears = findPermanents(player2, "Grizzly Bears");
        assertThat(bears).hasSize(2);
        assertThat(bears).allMatch(p -> p.getMarkedDamage() == 1);
    }

    @Test
    @DisplayName("Blocking does not trigger the ability")
    void blockingDoesNotTrigger() {
        addCreatureReady(player2, new SomberwaldVigilante());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The trigger kills a one-toughness blocker before combat damage")
    void killsBlockerBeforeCombatDamage() {
        Permanent vigilante = addCreatureReady(player1, new SomberwaldVigilante());
        vigilante.setAttacking(true);
        addCreatureReady(player2, new CathedralSanctifier());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, this::resolveAllTriggers);

        assertThat(findPermanents(player2, "Cathedral Sanctifier")).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getName()).contains("Cathedral Sanctifier");
        assertThat(vigilante.getMarkedDamage()).isZero();

        resolveCombat();

        assertThat(findPermanents(player1, "Somberwald Vigilante")).containsExactly(vigilante);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An unblocked attack does not trigger the ability")
    void unblockedAttackDoesNotTrigger() {
        Permanent vigilante = addCreatureReady(player1, new SomberwaldVigilante());
        vigilante.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        resolveCombat();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
