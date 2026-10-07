package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CartoucheOfSolidarity;
import com.github.laxika.magicalvibes.cards.d.DuneBeetle;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrespassersCurse.class, DuneBeetle.class, CartoucheOfSolidarity.class})
class TrespassersCurseTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast targeting an opponent, entering attached to that player")
    void castTargetingOpponent() {
        harness.setHand(player1, List.of(new TrespassersCurse()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve

        Permanent curse = findPermanent(player1, "Trespasser's Curse");
        assertThat(curse.getAttachedTo()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Enchanted player loses 1 life and you gain 1 life when their creature enters")
    void drainsWhenEnchantedPlayerCreatureEnters() {
        addCurseOnPlayer2();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new DuneBeetle()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castCreature(player2, 0);

        harness.passBothPriorities(); // resolve creature spell and queue Curse trigger
        harness.passBothPriorities(); // resolve the drain trigger

        harness.assertLife(player2, 19); // enchanted player loses 1
        harness.assertLife(player1, 21); // curse controller gains 1
    }

    @Test
    @DisplayName("Does not trigger when a creature the curse controller controls enters")
    void doesNotTriggerForCurseControllerCreature() {
        addCurseOnPlayer2();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new DuneBeetle()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).isEmpty(); // no drain trigger queued
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can enchant yourself and lose then regain life for your own creature")
    void canEnchantSelf() {
        harness.setLife(player1, 1);
        harness.setHand(player1, List.of(new TrespassersCurse(), new DuneBeetle()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castEnchantment(player1, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Trespasser's Curse").getAttachedTo()).isEqualTo(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.assertLife(player1, 1);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Each Curse triggers for a creature token, but not for the Aura creating it")
    void eachCurseTriggersForToken() {
        addCurseOnPlayer2();
        addCurseOnPlayer2();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DuneBeetle());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CartoucheOfSolidarity()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castEnchantment(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A queued drain still resolves after the Curse leaves the battlefield")
    void triggerSurvivesCurseLeaving() {
        addCurseOnPlayer2();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DuneBeetle()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        Permanent curse = findPermanent(player1, "Trespasser's Curse");
        gd.playerBattlefields.get(player1.getId()).remove(curse);
        gd.playerGraveyards.get(player1.getId()).add(curse.getCard());

        resolveAllTriggers();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    private void addCurseOnPlayer2() {
        Permanent curse = harness.addToBattlefieldAndReturn(player1, new TrespassersCurse());
        curse.setAttachedTo(player2.getId());
    }
}
