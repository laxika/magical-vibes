package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.b.BarbarianGuides;
import com.github.laxika.magicalvibes.cards.k.KjeldoranSkyknight;
import com.github.laxika.magicalvibes.cards.p.PygmyAllosaurus;
import com.github.laxika.magicalvibes.cards.r.RimeDryad;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StaffOfTheAges.class, Shatter.class, SnowCoveredForest.class, Swamp.class,
        BalduvianBears.class, BarbarianGuides.class, KjeldoranSkyknight.class,
        PygmyAllosaurus.class, RimeDryad.class})
class StaffOfTheAgesTest extends BaseCardTest {

    @Test
    @DisplayName("Without the Staff, a swampwalking attacker can't be blocked through a Swamp")
    void swampwalkStopsBlockWithoutStaff() {
        harness.addToBattlefield(player2, new Swamp());
        Permanent attacker = addSwampwalker(player1);
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("With the Staff out, a swampwalking attacker can be blocked through a Swamp")
    void staffLetsSwampwalkerBeBlocked() {
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new StaffOfTheAges());
        Permanent attacker = addSwampwalker(player1);
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());
        harness.setLife(player2, 20);

        prepareDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The Staff also switches off snow landwalk")
    void staffSwitchesOffSnowLandwalk() {
        addSnowForest(player2);
        harness.addToBattlefield(player2, new StaffOfTheAges());
        Permanent dryad = addCreatureReady(player1, new RimeDryad());
        dryad.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());
        harness.setLife(player2, 20);

        prepareDeclareBlockers();
        declareBlock(blocker, dryad);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The Staff switches off temporary snow landwalk even when its controller is attacking")
    void staffSwitchesOffTemporarySnowLandwalk() {
        addSnowForest(player2);
        harness.addToBattlefield(player1, new StaffOfTheAges());
        Permanent guides = addCreatureReady(player1, new BarbarianGuides());
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(guides), 0, null, attacker.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "FOREST");
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());

        prepareDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The Staff leaves non-landwalk evasion alone")
    void staffDoesNotAffectFlying() {
        harness.addToBattlefield(player2, new StaffOfTheAges());
        Permanent attacker = addCreatureReady(player1, new KjeldoranSkyknight());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped Staff still allows landwalking creatures to be blocked")
    void tappedStaffStillAllowsBlocking() {
        harness.addToBattlefield(player2, new Swamp());
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new StaffOfTheAges());
        staff.tap();
        Permanent attacker = addSwampwalker(player1);
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());
        harness.setLife(player2, 20);

        prepareDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Destroying the Staff restores landwalk before blockers are declared")
    void destroyingStaffRestoresLandwalk() {
        harness.addToBattlefield(player2, new Swamp());
        Permanent staff = harness.addToBattlefieldAndReturn(player2, new StaffOfTheAges());
        Permanent attacker = addCreatureReady(player1, new PygmyAllosaurus());
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, staff.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Staff of the Ages");
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Snow forestwalk prevents blocking when the Staff is absent")
    void snowForestwalkStopsBlockingWithoutStaff() {
        addSnowForest(player2);
        Permanent attacker = addCreatureReady(player1, new RimeDryad());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }

    private Permanent addSwampwalker(Player player) {
        Permanent perm = addCreatureReady(player, new PygmyAllosaurus());
        perm.setAttacking(true);
        return perm;
    }

    private void addSnowForest(Player player) {
        harness.addToBattlefield(player, new SnowCoveredForest());
    }

}
