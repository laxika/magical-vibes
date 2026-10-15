package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AjaniGoldmane;
import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.l.LoxodonHierarch;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Searing Meditation")
@CardUsed({SearingMeditation.class, LoxodonHierarch.class, BorosRecruit.class, AjaniGoldmane.class})
class SearingMeditationTest extends BaseCardTest {

    @Test
    @DisplayName("Pays {2} to deal 2 damage to any target")
    void paysAndDealsDamageToAnyTarget() {
        harness.addToBattlefield(player1, new SearingMeditation());
        harness.castFromHand(player1, new LoxodonHierarch(), "{2}{G}{W}");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, lifeBefore - 2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can deal the damage to a creature")
    void dealsDamageToCreature() {
        harness.addToBattlefield(player1, new SearingMeditation());
        Permanent target = addCreatureReady(player2, new BorosRecruit());
        harness.castFromHand(player1, new LoxodonHierarch(), "{2}{G}{W}");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        resolveAllTriggers();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Boros Recruit");
        harness.assertInGraveyard(player2, "Boros Recruit");
    }

    @Test
    @DisplayName("Declining the payment deals no damage")
    void decliningDoesNothing() {
        harness.addToBattlefield(player1, new SearingMeditation());
        harness.castFromHand(player1, new LoxodonHierarch(), "{2}{G}{W}");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player2, lifeBefore);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Gaining life from another player does not trigger it")
    void doesNotTriggerForOpponentLifeGain() {
        harness.addToBattlefield(player1, new SearingMeditation());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new LoxodonHierarch(), "{2}{G}{W}");
        resolveAllTriggers();

        harness.assertLife(player2, lifeBefore + 4);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Accepting without enough mana deals no damage")
    void acceptingWithoutEnoughManaDoesNothing() {
        harness.addToBattlefield(player1, new SearingMeditation());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castFromHand(player1, new LoxodonHierarch(), "{2}{G}{W}");
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, lifeBefore);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target a planeswalker")
    @CardUsed({SearingMeditation.class, LoxodonHierarch.class, AjaniGoldmane.class})
    void canTargetPlaneswalker() {
        harness.addToBattlefield(player1, new SearingMeditation());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AjaniGoldmane());
        target.setCounterCount(CounterType.LOYALTY, 4);
        harness.castFromHand(player1, new LoxodonHierarch(), "{2}{G}{W}");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        resolveAllTriggers();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can deal damage to its controller")
    void canTargetController() {
        harness.addToBattlefield(player1, new SearingMeditation());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.castFromHand(player1, new LoxodonHierarch(), "{2}{G}{W}");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, lifeBefore + 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Triggers once per life gain event, including repeated events in one turn")
    void triggersForEachLifeGainEvent() {
        harness.addToBattlefield(player1, new SearingMeditation());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        for (int event = 0; event < 2; event++) {
            harness.castFromHand(player1, new LoxodonHierarch(), "{2}{G}{W}");
            harness.addMana(player1, ManaColor.COLORLESS, 2);
            resolveAllTriggers();
            harness.handlePermanentChosen(player1, player2.getId());
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            harness.assertLife(player2, lifeBefore - 2 * (event + 1));
            assertThat(gd.stack).isEmpty();
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
        }
    }
}
