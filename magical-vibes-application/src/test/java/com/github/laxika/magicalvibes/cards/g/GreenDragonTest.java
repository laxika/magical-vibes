package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GreenDragon.class, GrizzlyBears.class, HillGiant.class, Shock.class, DoomBlade.class})
class GreenDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Poison Breath destroys an opponent's creature that survives damage")
    void destroysDamagedOpponentCreature() {
        harness.addToBattlefield(player2, new HillGiant());
        castGreenDragonWithShock();

        UUID hillGiantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, hillGiantId);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Poison Breath does not affect a creature controlled by Green Dragon's controller")
    void doesNotDestroyOwnCreature() {
        harness.addToBattlefield(player1, new HillGiant());
        castGreenDragonWithShock();

        UUID hillGiantId = harness.getPermanentId(player1, "Hill Giant");
        harness.castInstant(player1, 0, hillGiantId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Poison Breath remains active after Green Dragon leaves the battlefield")
    void remainsActiveAfterSourceLeaves() {
        Permanent dragon = castGreenDragon();
        harness.addToBattlefield(player2, new HillGiant());

        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, dragon.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Green Dragon");

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Hill Giant"));
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Poison Breath expires at end of turn")
    void expiresAtEndOfTurn() {
        harness.addToBattlefield(player2, new HillGiant());
        castGreenDragonWithShock();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Hill Giant"));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Poison Breath triggers for combat damage")
    void triggersForCombatDamage() {
        castGreenDragon();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());

        Permanent attacker = findPermanent(player1, "Grizzly Bears");
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = findPermanent(player2, "Hill Giant");
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();


        harness.assertInGraveyard(player2, "Hill Giant");
    }

    private Permanent castGreenDragon() {
        harness.setHand(player1, List.of(new GreenDragon(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Green Dragon");
    }

    private void castGreenDragonWithShock() {
        castGreenDragon();
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
