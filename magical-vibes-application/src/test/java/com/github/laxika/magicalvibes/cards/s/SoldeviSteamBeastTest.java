package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SoldeviSteamBeastTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking taps the Beast and gives the opponent 2 life")
    void attackingGivesOpponentTwoLife() {
        addCreatureReady(player1, new SoldeviSteamBeast());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        // 20 + 2 from the tap trigger - 4 unblocked combat damage.
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Another creature of the controller becoming tapped does not trigger the Beast")
    void otherCreatureTappingDoesNotTrigger() {
        addCreatureReady(player1, new SoldeviSteamBeast());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(1));
        harness.passBothPriorities();

        // 20 - 2 combat damage only; no life gained because the Beast stayed untapped.
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("{2} regenerates the Beast from lethal combat damage")
    void regeneratesFromLethalDamage() {
        addCreatureReady(player1, new SoldeviSteamBeast());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent beast = findPermanent(player1, "Soldevi Steam Beast");
        assertThat(beast.getRegenerationShield()).isEqualTo(1);

        beast.setBlocking(true);
        beast.addBlockingTarget(0);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Soldevi Steam Beast");
        Permanent regenerated = findPermanent(player1, "Soldevi Steam Beast");
        assertThat(regenerated.isTapped()).isTrue();
        assertThat(regenerated.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Without a shield the Beast dies to lethal combat damage")
    void diesWithoutShield() {
        Permanent beast = addCreatureReady(player1, new SoldeviSteamBeast());

        beast.setBlocking(true);
        beast.addBlockingTarget(0);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Soldevi Steam Beast");
    }
}
