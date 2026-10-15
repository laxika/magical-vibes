package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.cards.f.FightingDrake;
import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Kezzerdrix.class, FightingDrake.class, HornedTurtle.class, DarkBanishing.class})
class KezzerdrixTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to its controller at upkeep when opponents control no creatures")
    void damagesControllerWhenOpponentHasNoCreatures() {
        harness.addToBattlefield(player1, new Kezzerdrix());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife - 4);
    }

    @Test
    @DisplayName("Does not damage its controller while an opponent controls a creature")
    void noDamageWhenOpponentControlsCreature() {
        harness.addToBattlefield(player1, new Kezzerdrix());
        harness.addToBattlefield(player2, new HornedTurtle());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("Controller's own creatures do not stop the trigger")
    void controllerCreaturesDoNotStopTrigger() {
        harness.addToBattlefield(player1, new Kezzerdrix());
        harness.addToBattlefield(player1, new HornedTurtle());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife - 4);
    }

    @Test
    @DisplayName("Does not damage its controller if an opponent gains a creature before resolution")
    void noDamageWhenOpponentGainsCreatureBeforeResolution() {
        harness.addToBattlefield(player1, new Kezzerdrix());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.addToBattlefield(player2, new HornedTurtle());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("First strike prevents a dying blocker from dealing regular combat damage")
    void firstStrikePreventsDyingBlockerFromDealingRegularDamage() {
        Permanent attacker = addCreatureReady(player1, new Kezzerdrix());
        Permanent blocker = addCreatureReady(player2, new FightingDrake());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        resolveCombat();

        harness.assertInGraveyard(player2, "Fighting Drake");
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Removing the opponent's last creature after upkeep begins does not create a trigger")
    void noTriggerWhenOpponentCreatureLeavesAfterUpkeepBegins() {
        harness.addToBattlefield(player1, new Kezzerdrix());
        harness.addToBattlefield(player2, new HornedTurtle());
        harness.setHand(player1, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Horned Turtle"));

        harness.assertInGraveyard(player2, "Horned Turtle");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new Kezzerdrix());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
    }
}
