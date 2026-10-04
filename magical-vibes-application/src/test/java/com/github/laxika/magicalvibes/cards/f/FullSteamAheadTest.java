package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.o.OasisGardener;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FullSteamAhead.class, OasisGardener.class, Frogify.class})
class FullSteamAheadTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts your creatures and grants trample")
    void boostsOwnCreaturesAndGrantsTrample() {
        Permanent ownCreature = addCreatureReady(player1, new OasisGardener());
        Permanent opponentCreature = addCreatureReady(player2, new OasisGardener());

        castFullSteamAhead();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Your creatures can't be blocked by more than one creature")
    void limitsBlockers() {
        addCreatureReady(player1, new OasisGardener());
        addCreatureReady(player2, new OasisGardener());
        addCreatureReady(player2, new OasisGardener());

        castFullSteamAhead();
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
    }

    @Test
    @DisplayName("The temporary blocker restriction wears off at end of turn")
    void blockerRestrictionWearsOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new OasisGardener());

        castFullSteamAhead();
        assertThat(gqs.getMaxBlockersAllowed(gd, creature)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getMaxBlockersAllowed(gd, creature)).isEqualTo(Integer.MAX_VALUE);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void creaturesEnteringAfterResolutionAreUnaffected() {
        castFullSteamAhead();
        Permanent lateCreature = addCreatureReady(player1, new OasisGardener());

        assertThat(gqs.getEffectivePower(gd, lateCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lateCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lateCreature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getMaxBlockersAllowed(gd, lateCreature)).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void oneBlockerIsLegalAndExcessDamageTramplesOver() {
        addCreatureReady(player1, new OasisGardener());
        Permanent blocker = addCreatureReady(player2, new OasisGardener());
        harness.setLife(player2, 20);
        castFullSteamAhead();
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2, player2.getId(), 2));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertOnBattlefield(player1, "Oasis Gardener");
        harness.assertInGraveyard(player2, "Oasis Gardener");
    }

    @Test
    @CardUsed(Frogify.class)
    void losingAbilitiesRemovesGrantedBlockerRestriction() {
        Permanent creature = addCreatureReady(player1, new OasisGardener());
        castFullSteamAhead();
        harness.setHand(player1, List.of(new Frogify()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getMaxBlockersAllowed(gd, creature)).isEqualTo(Integer.MAX_VALUE);
    }

    private void castFullSteamAhead() {
        harness.setHand(player1, List.of(new FullSteamAhead()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
