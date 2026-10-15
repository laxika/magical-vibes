package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GuerrillaGorilla;
import com.github.laxika.magicalvibes.cards.e.EpicFight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RedGuardianSuperSoldier.class, GuerrillaGorilla.class, EpicFight.class})
class RedGuardianSuperSoldierTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys an opponent's creature that dealt damage this turn")
    void etbDestroysOpponentCreatureThatDealtDamage() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GuerrillaGorilla());
        gd.recordDamageDealtBySource(bears.getId(), 2);

        castGuardian();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Guerrilla Gorilla");
        harness.assertInGraveyard(player2, "Guerrilla Gorilla");
    }

    @Test
    @DisplayName("ETB cannot target a creature controlled by its controller")
    void etbDoesNotDestroyOwnCreatureEvenIfItDealtDamage() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GuerrillaGorilla());
        gd.recordDamageDealtBySource(ownBears.getId(), 2);
        harness.addToBattlefield(player2, new GuerrillaGorilla());

        castGuardian();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownBears);
        harness.assertOnBattlefield(player2, "Guerrilla Gorilla");
    }

    @Test
    @DisplayName("ETB does not destroy an opponent's creature that dealt no damage this turn")
    void etbDoesNotDestroyOpponentCreatureThatDealtNoDamage() {
        harness.addToBattlefield(player2, new GuerrillaGorilla());

        castGuardian();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        harness.assertOnBattlefield(player2, "Guerrilla Gorilla");
    }

    @Test
    void flashCanDestroyCreatureAfterCombatDamageOnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GuerrillaGorilla());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.resolveCombatDamage();
        harness.assertLife(player1, 18);

        castGuardian();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Guerrilla Gorilla");
        harness.assertOnBattlefield(player1, "Red Guardian, Super-Soldier");
    }

    @Test
    void destroysCreatureThatDealtOnlyNoncombatDamageToAnotherCreature() {
        Permanent fighter = harness.addToBattlefieldAndReturn(player2, new GuerrillaGorilla());
        Permanent victim = harness.addToBattlefieldAndReturn(player1, new GuerrillaGorilla());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new EpicFight()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castModalSorceryWithModes(player2, 0, 1, 2,
                new int[]{0, 1}, List.of(fighter.getId(), fighter.getId(), victim.getId()), null);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Guerrilla Gorilla");
        harness.assertInGraveyard(player1, "Guerrilla Gorilla");

        castGuardian();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, fighter.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Guerrilla Gorilla");
    }

    @Test
    void targetBecomingControlledByGuardianControllerIsNotDestroyed() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GuerrillaGorilla());
        gd.recordDamageDealtBySource(target.getId(), 2);
        castGuardian();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        harness.assertNotInGraveyard(player2, "Guerrilla Gorilla");
    }

    @Test
    void damageFromPreviousTurnDoesNotMakeCreatureEligible() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GuerrillaGorilla());
        gd.recordDamageDealtBySource(target.getId(), 2);
        harness.passUntil(player2, TurnStep.UPKEEP);

        castGuardian();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Guerrilla Gorilla");
        harness.assertOnBattlefield(player1, "Red Guardian, Super-Soldier");
        assertThat(gd.stack).isEmpty();
    }

    private void castGuardian() {
        harness.castFromHand(player1, new RedGuardianSuperSoldier(), "{2}{W}");
    }
}
