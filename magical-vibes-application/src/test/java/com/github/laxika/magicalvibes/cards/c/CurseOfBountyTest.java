package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CurseOfBounty.class, Forest.class, GrizzlyBears.class, JaceBeleren.class})
class CurseOfBountyTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps nonlands controlled by the Curse controller and attacking opponent")
    void untapsControllerAndAttackingOpponentsNonlands() {
        placeCurseOnPlayer1();
        Permanent controllerCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent controllerLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        controllerCreature.tap();
        controllerLand.tap();

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        Permanent attackerLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        attackerLand.tap();

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(controllerCreature.isTapped()).isFalse();
        assertThat(controllerLand.isTapped()).isTrue();
        assertThat(attacker.isTapped()).isFalse();
        assertThat(attackerLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not untap the enchanted player's permanents when the Curse controller attacks")
    void controllerAttackingEnchantedPlayerDoesNotUntapEnchantedPlayersPermanents() {
        placeCurseOnPlayer(player2);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent enchantedPlayersCreature = addCreatureReady(player2, new GrizzlyBears());
        enchantedPlayersCreature.tap();

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(attacker.isTapped()).isFalse();
        assertThat(enchantedPlayersCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attacking the enchanted player's planeswalker does not trigger")
    void attackingPlaneswalkerDoesNotTrigger() {
        placeCurseOnPlayer1();
        Permanent controllerCreature = addCreatureReady(player1, new GrizzlyBears());
        controllerCreature.tap();
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(0), Map.of(0, planeswalker.getId()));
        resolveAllTriggers();

        assertThat(controllerCreature.isTapped()).isTrue();
    }

    private void placeCurseOnPlayer1() {
        placeCurseOnPlayer(player1);
    }

    private void placeCurseOnPlayer(com.github.laxika.magicalvibes.model.Player enchantedPlayer) {
        Permanent curse = harness.addToBattlefieldAndReturn(player1, new CurseOfBounty());
        curse.setAttachedTo(enchantedPlayer.getId());
    }
}
