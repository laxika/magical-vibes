package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.p.PlatedSlagwurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Duskworker.class, PlatedSlagwurm.class})
class DuskworkerTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked grants Duskworker one regeneration shield")
    void becomingBlockedGrantsRegenerationShield() {
        Permanent duskworker = addCreatureReady(player1, new Duskworker());
        addCreatureReady(player2, new PlatedSlagwurm());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();

        assertThat(gd.regenerationShieldCount(duskworker.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Becoming blocked lets Duskworker survive lethal combat damage")
    void becomingBlockedRegeneratesFromLethalCombatDamage() {
        Permanent duskworker = addCreatureReady(player1, new Duskworker());
        addCreatureReady(player2, new PlatedSlagwurm());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        assertThat(gd.regenerationShieldCount(duskworker.getId())).isEqualTo(1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Duskworker");
        Permanent survivor = findPermanent(player1, "Duskworker");
        assertThat(survivor.isTapped()).isTrue();
        assertThat(gd.regenerationShieldCount(survivor.getId())).isZero();
    }

    @Test
    @DisplayName("Becoming blocked by multiple creatures grants only one regeneration shield")
    void becomingBlockedByMultipleCreaturesTriggersOnce() {
        Permanent duskworker = addCreatureReady(player1, new Duskworker());
        addCreatureReady(player2, new PlatedSlagwurm());
        addCreatureReady(player2, new PlatedSlagwurm());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        harness.passBothPriorities();

        assertThat(gd.regenerationShieldCount(duskworker.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Three generic mana gives Duskworker +1/+0 until end of turn")
    void activatedAbilityBoostsUntilEndOfTurn() {
        Permanent duskworker = addCreatureReady(player1, new Duskworker());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, duskworker)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, duskworker)).isEqualTo(0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, duskworker)).isZero();
    }

    @Test
    @DisplayName("Repeated activations work while tapped and boost only their source")
    void repeatedActivationsBoostOnlyTappedSource() {
        Permanent duskworker = addCreatureReady(player1, new Duskworker());
        Permanent other = addCreatureReady(player1, new Duskworker());
        duskworker.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, duskworker)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, duskworker)).isZero();
        assertThat(duskworker.isTapped()).isTrue();
        assertThat(PermanentGrants.powerBoostOf(gd, other)).isZero();
        assertThat(gd.regenerationShieldCount(other.getId())).isZero();
    }

    @Test
    @DisplayName("Blocking does not regenerate Duskworker")
    void blockingDoesNotGrantRegeneration() {
        addCreatureReady(player1, new PlatedSlagwurm());
        Permanent duskworker = addCreatureReady(player2, new Duskworker());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.regenerationShieldCount(duskworker.getId())).isZero();
        resolveCombat();

        harness.assertNotOnBattlefield(player2, "Duskworker");
        harness.assertInGraveyard(player2, "Duskworker");
    }
}
