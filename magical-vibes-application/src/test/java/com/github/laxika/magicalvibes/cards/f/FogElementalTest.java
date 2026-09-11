package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.ArdentMilitia;
import com.github.laxika.magicalvibes.cards.a.Avizoa;
import com.github.laxika.magicalvibes.model.ManaColor;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArdentMilitia.class, Avizoa.class, FogElemental.class})
class FogElementalTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Fog Elemental puts it on the stack")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new FogElemental(), "{2}{U}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard()).isInstanceOf(FogElemental.class);
    }

    @Test
    @DisplayName("Resolving puts Fog Elemental onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.castFromHand(player1, new FogElemental(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof FogElemental);
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new FogElemental()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Fog Elemental enters battlefield with summoning sickness")
    void entersBattlefieldWithSummoningSickness() {
        harness.castFromHand(player1, new FogElemental(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement()
                .satisfies(permanent -> assertThat(permanent.isSummoningSick()).isTrue());
    }

    // ===== Attack trigger pushes onto stack =====

    @Test
    @DisplayName("Declaring Fog Elemental as attacker pushes a triggered ability onto the stack")
    void attackTriggerPushesOntoStack() {
        Permanent fogPerm = addCreatureReady(player1, new FogElemental());
        declareAttackers(List.of(0));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getCard()).isInstanceOf(FogElemental.class);
        assertThat(entry.getSourcePermanentId()).isEqualTo(fogPerm.getId());
    }

    // ===== Block trigger pushes onto stack =====

    @Test
    @DisplayName("Declaring Fog Elemental as blocker pushes a triggered ability onto the stack")
    void blockTriggerPushesOntoStack() {
        Permanent fogPerm = addCreatureReady(player2, new FogElemental());
        Permanent atkPerm = addCreatureReady(player1, new Avizoa());
        atkPerm.setAttacking(true);
        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getCard()).isInstanceOf(FogElemental.class);
        assertThat(entry.getSourcePermanentId()).isEqualTo(fogPerm.getId());
    }

    // ===== Sacrificed at end of combat when attacking =====

    @Test
    @DisplayName("Fog Elemental is sacrificed at end of combat after attacking")
    void sacrificedAtEndOfCombatWhenAttacking() {
        harness.setLife(player2, 20);

        addCreatureReady(player1, new FogElemental());
        declareAttackers(List.of(0));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof FogElemental);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof FogElemental);
    }

    @Test
    @DisplayName("Fog Elemental deals combat damage before being sacrificed")
    void dealsDamageBeforeSacrifice() {
        harness.setLife(player2, 20);

        addCreatureReady(player1, new FogElemental());
        declareAttackers(List.of(0));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof FogElemental);
    }

    // ===== Sacrificed at end of combat when blocking =====

    @Test
    @DisplayName("Fog Elemental is sacrificed at end of combat after blocking")
    void sacrificedAtEndOfCombatWhenBlocking() {
        addCreatureReady(player2, new FogElemental());

        // Use a small creature so Fog Elemental survives combat damage
        Permanent atkPerm = addCreatureReady(player1, new Avizoa());
        atkPerm.setAttacking(true);
        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        // Fog Elemental should be in graveyard (sacrificed at end of combat)
        harness.assertNotOnBattlefield(player2, "Fog Elemental");
        harness.assertInGraveyard(player2, "Fog Elemental");
        // Avizoa should also be dead from combat damage (4 power vs 2 toughness)
        harness.assertInGraveyard(player1, "Avizoa");
    }

    // ===== Not sacrificed if removed before end of combat =====

    @Test
    @DisplayName("Fog Elemental is not sacrificed if removed from battlefield before trigger resolves")
    void notSacrificedIfRemovedBeforeTriggerResolves() {
        Permanent fogPerm = addCreatureReady(player1, new FogElemental());
        declareAttackers(List.of(0));

        harness.inMutationScope(() -> assertThat(
                harness.getPermanentRemovalService().removePermanentToHand(gd, fogPerm)).isTrue());

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerHands.get(player1.getId())).contains(fogPerm.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card == fogPerm.getCard());
    }

    // ===== Normal creatures don't trigger on attack =====

    @Test
    @DisplayName("Normal creature attacking does not push any trigger onto the stack")
    void normalCreatureDoesNotTriggerOnAttack() {
        addCreatureReady(player1, new Avizoa());
        declareAttackers(List.of(0));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Fog Elemental's flying prevents a nonflying creature from blocking it")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        Permanent fogPerm = addCreatureReady(player1, new FogElemental());
        Permanent blocker = addCreatureReady(player2, new ArdentMilitia());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(fogPerm);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Fog Elemental can be blocked by a creature with flying")
    void flyingCreatureCanBlockFogElemental() {
        Permanent fogPerm = addCreatureReady(player1, new FogElemental());
        Permanent blocker = addCreatureReady(player2, new Avizoa());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(fogPerm);
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Fog Elemental that neither attacks nor blocks is not sacrificed")
    void notSacrificedWhenNotInCombat() {
        addCreatureReady(player1, new FogElemental());

        declareAttackers(List.of());
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        harness.assertOnBattlefield(player1, "Fog Elemental");
    }

    // ===== Game log =====

    @Test
    @DisplayName("Attack trigger generates appropriate game log entries")
    void attackTriggerGeneratesLogEntries() {
        addCreatureReady(player1, new FogElemental());
        declareAttackers(List.of(0));

        assertThat(gameLogContains("'s attack ability triggers.")).isTrue();

        harness.passBothPriorities();

        assertThat(gameLogContains(" is sacrificed.")).isTrue();
    }
}

