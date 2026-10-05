package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InstigatorGang.class, WalkingCorpse.class})
class InstigatorGangTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms to Wildblood Pack when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        harness.addToBattlefield(player1, new InstigatorGang());
        Permanent gang = findPermanent(player1, "Instigator Gang");

        // spellsCastLastTurn is empty (no spells cast)
        gd.spellsCastLastTurn.clear();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve triggered ability

        assertThat(gang.isTransformed()).isTrue();
        assertThat(gang.getCard().getName()).isEqualTo("Wildblood Pack");
        assertThat(gqs.getEffectivePower(gd, gang)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, gang)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        harness.addToBattlefield(player1, new InstigatorGang());
        Permanent gang = findPermanent(player1, "Instigator Gang");

        // Simulate that a spell was cast last turn
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        advanceToUpkeep(player1);

        assertThat(gang.isTransformed()).isFalse();
        assertThat(gang.getCard().getName()).isEqualTo("Instigator Gang");
    }

    @Test
    @DisplayName("Wildblood Pack transforms back when a player cast two or more spells last turn")
    void wildbloodTransformsBackWhenTwoSpellsCast() {
        harness.addToBattlefield(player1, new InstigatorGang());
        Permanent gang = findPermanent(player1, "Instigator Gang");

        // Transform to Wildblood Pack first
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve transform
        assertThat(gang.isTransformed()).isTrue();

        // Now simulate that a player cast 2+ spells last turn
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve transform back

        assertThat(gang.isTransformed()).isFalse();
        assertThat(gang.getCard().getName()).isEqualTo("Instigator Gang");
        assertThat(gqs.getEffectivePower(gd, gang)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gang)).isEqualTo(3);
    }

    @Test
    @DisplayName("Wildblood Pack does not transform back when only one spell was cast last turn")
    void wildbloodDoesNotTransformWhenOneSpellCast() {
        harness.addToBattlefield(player1, new InstigatorGang());
        Permanent gang = findPermanent(player1, "Instigator Gang");

        // Transform to Wildblood Pack first
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gang.isTransformed()).isTrue();

        // Only 1 spell cast last turn by each player
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        advanceToUpkeep(player2);

        assertThat(gang.isTransformed()).isTrue();
        assertThat(gang.getCard().getName()).isEqualTo("Wildblood Pack");
    }

    @Test
    @DisplayName("Transform triggers on opponent's upkeep too")
    void transformTriggersOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new InstigatorGang());
        Permanent gang = findPermanent(player1, "Instigator Gang");

        // No spells cast last turn
        gd.spellsCastLastTurn.clear();

        // Trigger on opponent's upkeep (not player1's)
        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve

        assertThat(gang.isTransformed()).isTrue();
        assertThat(gang.getCard().getName()).isEqualTo("Wildblood Pack");
    }

    @Test
    @DisplayName("Attacking creatures you control get +1/+0 from front face")
    void frontFaceBoostsAttackingCreatures() {
        Permanent gang = addCreatureReady(player1, new InstigatorGang());
        Permanent corpse = addCreatureReady(player1, new WalkingCorpse()); // 2/2

        markAttacking(player1, List.of(0, 1));

        // Instigator Gang (2/3) attacking gets +1/+0 from its own static effect = 3/3
        assertThat(gqs.getEffectivePower(gd, gang)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, gang)).isEqualTo(3);

        // Walking Corpse (2/2) attacking gets +1/+0 = 3/2
        assertThat(gqs.getEffectivePower(gd, corpse)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, corpse)).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-attacking creatures do not get the boost")
    void nonAttackingCreaturesNotBoosted() {
        addCreatureReady(player1, new InstigatorGang());
        Permanent corpse = addCreatureReady(player1, new WalkingCorpse()); // 2/2

        // Only attack with Instigator Gang (index 0), not corpse
        markAttacking(player1, List.of(0));

        // The corpse is not attacking, should remain 2/2
        assertThat(gqs.getEffectivePower(gd, corpse)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, corpse)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's attacking creatures do not get the boost")
    void opponentAttackingCreaturesNotBoosted() {
        addCreatureReady(player1, new InstigatorGang());
        Permanent opponentCorpse = addCreatureReady(player2, new WalkingCorpse()); // 2/2

        markAttacking(player2, List.of(0));

        // Opponent's corpse attacking should not get the +1/+0
        assertThat(gqs.getEffectivePower(gd, opponentCorpse)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCorpse)).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking creatures you control get +3/+0 from back face")
    void backFaceBoostsAttackingCreatures() {
        harness.addToBattlefield(player1, new InstigatorGang());
        Permanent gang = findPermanent(player1, "Instigator Gang");
        gang.setSummoningSick(false);

        // Transform to Wildblood Pack
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve transform
        assertThat(gang.isTransformed()).isTrue();

        Permanent corpse = addCreatureReady(player1, new WalkingCorpse()); // 2/2

        markAttacking(player1, List.of(0, 1));

        // Wildblood Pack (5/5) attacking gets +3/+0 from its own static effect = 8/5
        assertThat(gqs.getEffectivePower(gd, gang)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, gang)).isEqualTo(5);

        // Walking Corpse (2/2) attacking gets +3/+0 = 5/2
        assertThat(gqs.getEffectivePower(gd, corpse)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, corpse)).isEqualTo(2);
    }

    @Test
    void frontFaceBoostsAttackersWhileItStaysBack() {
        Permanent gang = addCreatureReady(player1, new InstigatorGang());
        Permanent attacker = addCreatureReady(player1, new WalkingCorpse());

        declareAttackersAndPrepareBlockers(List.of(1));

        assertThat(gqs.getEffectivePower(gd, gang)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
    }

    @Test
    void multipleGangsBoostEachOtherWithoutDoubleCountingSelf() {
        Permanent first = addCreatureReady(player1, new InstigatorGang());
        Permanent second = addCreatureReady(player1, new InstigatorGang());

        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
    }

    @Test
    void backFaceOnlyBoostsItsControllersAttackersWhileItStaysBack() {
        Permanent gang = addCreatureReady(player1, new InstigatorGang());
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gang.isTransformed()).isTrue();

        Permanent attacker = addCreatureReady(player1, new WalkingCorpse());
        Permanent nonAttacker = addCreatureReady(player1, new WalkingCorpse());
        Permanent opponent = addCreatureReady(player2, new WalkingCorpse());
        declareAttackersAndPrepareBlockers(List.of(1));

        assertThat(gqs.getEffectivePower(gd, gang)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, nonAttacker)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
    }

    @Test
    void transformedPackTramplesWithItsOwnAttackBonus() {
        addCreatureReady(player1, new InstigatorGang());
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        Permanent blocker = addCreatureReady(player2, new WalkingCorpse());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 6));

        harness.assertLife(player2, 14);
        harness.assertInGraveyard(player2, "Walking Corpse");
        harness.assertOnBattlefield(player1, "Wildblood Pack");
    }

    private void markAttacking(Player player, List<Integer> attackerIndices) {
        List<Permanent> battlefield = gd.playerBattlefields.get(player.getId());
        for (int idx : attackerIndices) {
            battlefield.get(idx).setAttacking(true);
        }
    }

}
