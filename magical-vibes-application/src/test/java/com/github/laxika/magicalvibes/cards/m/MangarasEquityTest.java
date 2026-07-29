package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MangarasEquityTest extends BaseCardTest {

    private void addEquity(CardColor chosenColor) {
        Permanent equity = new Permanent(new MangarasEquity());
        equity.setChosenColor(chosenColor);
        gd.playerBattlefields.get(player2.getId()).add(equity);
    }

    private Permanent attackWith(Card creature) {
        harness.addToBattlefield(player1, creature);
        Permanent attacker = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals(creature.getName()))
                .findFirst().orElseThrow();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        return attacker;
    }

    @Test
    @DisplayName("A creature of the chosen color damaging you takes that much damage back")
    void combatDamageToControllerReflected() {
        addEquity(CardColor.RED);
        Permanent attacker = attackWith(new HillGiant()); // red 3/3

        harness.passBothPriorities(); // combat damage to player2, Equity trigger queued
        harness.passBothPriorities(); // Equity resolves: 3 damage to Hill Giant

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(attacker.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("A creature of another color damaging you is not punished")
    void otherColorNotReflected() {
        addEquity(CardColor.BLACK);
        Permanent attacker = attackWith(new HillGiant()); // red 3/3

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    private void addWall(CardColor color) {
        Card wall = new Card();
        wall.setName(color + " Wall");
        wall.setType(CardType.CREATURE);
        wall.setManaCost("{1}");
        wall.setColor(color);
        wall.setPower(0);
        wall.setToughness(5);

        Permanent blocker = new Permanent(wall);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
    }

    @Test
    @DisplayName("A creature of the chosen color damaging a white creature you control is punished")
    void damageToWhiteCreatureReflected() {
        addEquity(CardColor.RED);
        addWall(CardColor.WHITE);
        Permanent attacker = attackWith(new HillGiant()); // red 3/3

        harness.passBothPriorities(); // combat damage to the wall, Equity trigger queued
        harness.passBothPriorities(); // Equity resolves: 3 damage to Hill Giant

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(attacker.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Damage to a non-white creature you control is not punished")
    void damageToNonWhiteCreatureNotReflected() {
        addEquity(CardColor.RED);
        addWall(CardColor.GREEN);
        Permanent attacker = attackWith(new HillGiant()); // red 3/3

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Paying {1}{W} at upkeep keeps it on the battlefield")
    void payAtUpkeepKeepsIt() {
        harness.addToBattlefield(player1, new MangarasEquity());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Mangara's Equity");
    }

    @Test
    @DisplayName("Declining to pay at upkeep sacrifices it")
    void declineAtUpkeepSacrificesIt() {
        harness.addToBattlefield(player1, new MangarasEquity());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Mangara's Equity");
    }
}
