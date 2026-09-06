package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GargantuanGorilla;
import com.github.laxika.magicalvibes.cards.s.StormCrow;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BestialFury.class, GargantuanGorilla.class, StormCrow.class})
class BestialFuryTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield schedules a draw at the beginning of the next turn's upkeep")
    void entering_schedulesDrawAtNextUpkeep() {
        Permanent creature = addCreatureReady(player1, new GargantuanGorilla());
        castBestialFury(player1, creature);

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("The scheduled draw resolves at the beginning of the next turn's upkeep")
    void drawResolvesAtNextUpkeep() {
        Permanent creature = addCreatureReady(player1, new GargantuanGorilla());
        castBestialFury(player1, creature);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("The Aura's controller draws when it enchants an opponent's creature")
    void enteringOnOpponentCreature_schedulesDrawForAuraController() {
        Permanent creature = addCreatureReady(player2, new GargantuanGorilla());
        castBestialFury(player1, creature);

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("When enchanted creature becomes blocked, it gets +4/+0 and gains trample")
    void becomesBlocked_boostsAndGrantsTrample() {
        Permanent attacker = addCreatureReady(player1, new GargantuanGorilla());
        addBestialFuryAttachedTo(player1, attacker);
        attacker.setAttacking(true);

        addReadyStormCrow(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(4);
        assertThat(attacker.getToughnessModifier()).isZero();
        assertThat(attacker.getGrantedKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("The boost and trample wear off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new GargantuanGorilla());
        addBestialFuryAttachedTo(player1, attacker);
        attacker.setAttacking(true);

        addReadyStormCrow(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(attacker.getToughnessModifier()).isZero();
        assertThat(attacker.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("A creature blocked by multiple blockers gets only one boost")
    void becomesBlockedByMultipleBlockers_triggersOnce() {
        Permanent attacker = addCreatureReady(player1, new GargantuanGorilla());
        addBestialFuryAttachedTo(player1, attacker);
        attacker.setAttacking(true);

        addReadyStormCrow(player2);
        addReadyStormCrow(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(4);
        assertThat(attacker.getGrantedKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("The blocked trigger still uses the creature the Aura last enchanted if the Aura leaves")
    void becomesBlockedTrigger_resolvesAfterAuraLeaves() {
        Permanent attacker = addCreatureReady(player1, new GargantuanGorilla());
        Permanent aura = addBestialFuryAttachedTo(player1, attacker);
        attacker.setAttacking(true);

        addReadyStormCrow(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(4);
        assertThat(attacker.getGrantedKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("No trigger when the Aura is on the battlefield but not attached")
    void notAttached_noTrigger() {
        Permanent attacker = addCreatureReady(player1, new GargantuanGorilla());
        addBestialFury(player1);
        attacker.setAttacking(true);

        addReadyStormCrow(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Bestial Fury"));
    }

    private Permanent addBestialFury(Player player) {
        Permanent perm = new Permanent(new BestialFury());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addBestialFuryAttachedTo(Player player, Permanent creature) {
        Permanent perm = addBestialFury(player);
        perm.setAttachedTo(creature.getId());
        return perm;
    }

    private Permanent addReadyStormCrow(Player player) {
        Permanent perm = new Permanent(new StormCrow());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void castBestialFury(Player caster, Permanent creature) {
        harness.setHand(caster, List.of(new BestialFury()));
        harness.addMana(caster, ManaColor.RED, 3);
        harness.castEnchantment(caster, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
