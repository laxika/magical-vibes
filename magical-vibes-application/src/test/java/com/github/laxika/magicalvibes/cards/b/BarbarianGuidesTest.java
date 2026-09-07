package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredSwamp;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        BarbarianGuides.class,
        BalduvianBears.class,
        SnowCoveredForest.class,
        SnowCoveredSwamp.class,
        Swamp.class
})
class BarbarianGuidesTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature can't be blocked while the defender controls a snow land of the chosen type")
    void grantsSnowLandwalkOfChosenType() {
        harness.addToBattlefield(player2, new SnowCoveredSwamp());
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());
        Permanent attacker = readyAttacker(player1);

        grantSnowLandwalk(attacker, "SWAMP");

        prepareDeclareBlockers();
        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("A non-snow land of the chosen type does not turn the landwalk on")
    void nonSnowLandDoesNotGrantEvasion() {
        harness.addToBattlefield(player2, new Swamp());
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());
        Permanent attacker = readyAttacker(player1);
        harness.setLife(player2, 20);

        grantSnowLandwalk(attacker, "SWAMP");

        prepareDeclareBlockers();
        declareBlock(blocker, attacker);
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A snow land of a different type does not turn the landwalk on")
    void snowLandOfOtherTypeDoesNotGrantEvasion() {
        harness.addToBattlefield(player2, new SnowCoveredForest());
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());
        Permanent attacker = readyAttacker(player1);
        harness.setLife(player2, 20);

        grantSnowLandwalk(attacker, "SWAMP");

        prepareDeclareBlockers();
        declareBlock(blocker, attacker);
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The snow-landwalking attacker is not offered as a blockable attacker")
    void snowLandwalkerIsNotOfferedAsBlockableAttacker() {
        harness.addToBattlefield(player2, new SnowCoveredSwamp());
        addCreatureReady(player2, new BalduvianBears());
        Permanent landwalker = readyAttacker(player1);
        Permanent plainAttacker = addCreatureReady(player1, new BalduvianBears());

        grantSnowLandwalk(landwalker, "SWAMP");
        plainAttacker.setAttacking(true);

        // The prompt path (which attackers the defender may be asked to block) must agree with the
        // enforcement path: only the attacker without snow swampwalk is offered.
        List<Integer> blockable = harness.getCombatBlockService()
                .getBlockableAttackerIndices(gd, player1.getId(), player2.getId());

        assertThat(blockable)
                .contains(indexOf(player1, plainAttacker))
                .doesNotContain(indexOf(player1, landwalker));
    }

    @Test
    @DisplayName("Snow landwalk expires during cleanup")
    void snowLandwalkExpiresAtCleanup() {
        harness.addToBattlefield(player2, new SnowCoveredSwamp());
        addCreatureReady(player2, new BalduvianBears());
        Permanent attacker = readyAttacker(player1);

        grantSnowLandwalk(attacker, "SWAMP");

        prepareDeclareBlockers();
        assertThat(harness.getCombatBlockService()
                .getBlockableAttackerIndices(gd, player1.getId(), player2.getId()))
                .doesNotContain(indexOf(player1, attacker));

        harness.ensurePriority(player1);
        gd.playerAutoStopSteps.put(player1.getId(), Set.of(TurnStep.UPKEEP));
        gd.playerAutoStopSteps.put(player2.getId(), Set.of(TurnStep.UPKEEP));
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(attacker.isAttacking()).isTrue();
        prepareDeclareBlockers();
        assertThat(harness.getCombatBlockService()
                .getBlockableAttackerIndices(gd, player1.getId(), player2.getId()))
                .contains(indexOf(player1, attacker));
    }

    @Test
    @DisplayName("The target creature is returned to its owner's hand at the beginning of the next end step")
    void returnsTargetToHandAtEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        Permanent guides = addCreatureReady(player1, new BarbarianGuides());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, indexOf(player1, guides), 0, null, bears.getId());
        assertThat(guides.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "SWAMP");

        harness.assertOnBattlefield(player1, "Balduvian Bears");

        // Advance to the end step — the creature is bounced to its owner's hand.
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Balduvian Bears");
        harness.assertInHand(player1, "Balduvian Bears");
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        Permanent guides = addCreatureReady(player1, new BarbarianGuides());
        Permanent opponentBears = addCreatureReady(player2, new BalduvianBears());

        harness.addMana(player1, ManaColor.RED, 3);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, guides), 0, null, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    /**
     * Activates the ability targeting {@code attacker}, answers the land-type prompt, and flags the
     * creature as attacking. Resolving the ability advances the turn out of combat, so the attacking
     * flag is set afterwards and {@code prepareDeclareBlockers()} forces the step back.
     */
    private void grantSnowLandwalk(Permanent attacker, String landType) {
        Permanent guides = addCreatureReady(player1, new BarbarianGuides());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, indexOf(player1, guides), 0, null, attacker.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, landType);

        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, attacker))));
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    /** The creature that will attack; {@link #grantSnowLandwalk} flags it as attacking. */
    private Permanent readyAttacker(Player player) {
        return addCreatureReady(player, new BalduvianBears());
    }
}
