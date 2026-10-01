package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AzoriusHerald;
import com.github.laxika.magicalvibes.cards.c.Condemn;
import com.github.laxika.magicalvibes.cards.e.EntropicEidolon;
import com.github.laxika.magicalvibes.cards.v.VampireNighthawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({RainOfGore.class, Condemn.class, AzoriusHerald.class, EntropicEidolon.class,
        VampireNighthawk.class})
class RainOfGoreTest extends BaseCardTest {

    @Test
    @DisplayName("A spell causing its controller to gain life causes life loss instead")
    void spellControllerLifeGainBecomesLoss() {
        harness.addToBattlefield(player2, new RainOfGore());
        Permanent attacker = addCreatureReady(player1, new AzoriusHerald());
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Condemn()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, attacker.getId());

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("A spell causing another player to gain life is unaffected")
    void anotherPlayersLifeGainIsUnaffected() {
        harness.addToBattlefield(player1, new RainOfGore());
        Permanent attacker = addCreatureReady(player2, new AzoriusHerald());
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Condemn()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, attacker.getId());

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 21);
    }

    @Test
    @DisplayName("An ability causing its controller to gain life causes life loss instead")
    void abilityControllerLifeGainBecomesLoss() {
        harness.addToBattlefield(player2, new RainOfGore());
        harness.addToBattlefield(player1, new EntropicEidolon());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Combat damage lifelink is unaffected")
    void combatDamageLifelinkIsUnaffected() {
        addCreatureReady(player1, new VampireNighthawk());
        harness.addToBattlefield(player2, new RainOfGore());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }
}
