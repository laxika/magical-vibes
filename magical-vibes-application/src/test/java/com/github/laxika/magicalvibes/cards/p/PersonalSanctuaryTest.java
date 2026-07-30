package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PersonalSanctuaryTest extends BaseCardTest {

    @Test
    @DisplayName("Damage to the controller is prevented during the controller's turn")
    void preventsDamageDuringControllersTurn() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new PersonalSanctuary());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Damage to the controller is not prevented during an opponent's turn")
    void doesNotPreventDamageDuringOpponentsTurn() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new PersonalSanctuary());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Combat damage to the controller during an opponent's turn still goes through")
    void doesNotPreventCombatDamageOnOpponentsTurn() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new PersonalSanctuary());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player2, List.of(0));

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Damage to the controller's creatures is not prevented")
    void doesNotPreventDamageToOwnCreatures() {
        harness.addToBattlefield(player1, new PersonalSanctuary());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
