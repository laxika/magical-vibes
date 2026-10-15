package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.j.JadeMage;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.k.KrosanGrip;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

@CardUsed({CurseOfTheForsaken.class, JadeMage.class, JaceBeleren.class, DoomBlade.class, KrosanGrip.class})
class CurseOfTheForsakenTest extends BaseCardTest {

    @Test
    @DisplayName("A creature attacking the enchanted player makes its controller gain 1 life")
    void attackingCreatureControllerGainsLife() {
        placeCurseOnPlayer1();
        addCreatureReady(player2, new JadeMage());
        harness.setLife(player2, 20);

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        harness.assertLife(player2, 21);
    }

    @Test
    @DisplayName("Each creature attacking the enchanted player triggers separately")
    void eachAttackerTriggersSeparately() {
        placeCurseOnPlayer1();
        addCreatureReady(player2, new JadeMage());
        addCreatureReady(player2, new JadeMage());
        harness.setLife(player2, 20);

        declareAttackers(player2, List.of(0, 1));
        resolveAllTriggers();

        harness.assertLife(player2, 22);
    }

    @Test
    @DisplayName("Attacking the enchanted player's planeswalker does not trigger")
    void attackingPlaneswalkerDoesNotTrigger() {
        placeCurseOnPlayer1();
        addCreatureReady(player2, new JadeMage());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(0), Map.of(0, planeswalker.getId()));
        resolveAllTriggers();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The Curse can enchant an opponent and reward attacks against that player")
    void castCurseEnchantingOpponent() {
        harness.setHand(player1, List.of(new CurseOfTheForsaken()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();
        addCreatureReady(player1, new JadeMage());
        harness.setLife(player1, 20);

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Attacking a player who is not enchanted does not grant life")
    void attackingUnenchantedPlayerDoesNotTrigger() {
        placeCurseOnPlayer1();
        addCreatureReady(player1, new JadeMage());
        harness.setLife(player1, 20);

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("An attacker still grants life after being destroyed in response")
    void destroyedAttackerStillGrantsLife() {
        placeCurseOnPlayer1();
        Permanent attacker = addCreatureReady(player2, new JadeMage());
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        declareAttackers(player2, List.of(0));
        harness.castInstant(player2, 0, attacker.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Jade Mage");
        harness.assertLife(player2, 21);
    }

    @Test
    @DisplayName("Destroying the Curse does not stop its pending trigger")
    void destroyedCurseStillGrantsLife() {
        Permanent curse = placeCurseOnPlayer1();
        addCreatureReady(player2, new JadeMage());
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new KrosanGrip()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        declareAttackers(player2, List.of(0));
        harness.castInstant(player2, 0, curse.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Curse of the Forsaken");
        harness.assertLife(player2, 21);
    }

    private Permanent placeCurseOnPlayer1() {
        Permanent curse = harness.addToBattlefieldAndReturn(player1, new CurseOfTheForsaken());
        curse.setAttachedTo(player1.getId());
        return curse;
    }
}
