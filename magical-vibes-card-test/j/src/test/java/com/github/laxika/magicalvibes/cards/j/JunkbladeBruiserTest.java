package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JunkbladeBruiser.class, Shock.class})
class JunkbladeBruiserTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+1 when its controller expends four")
    void getsBoostWhenControllerExpendsFour() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new JunkbladeBruiser());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 4);

        for (int i = 0; i < 4; i++) {
            harness.castAndResolveInstant(player1, 0, player2.getId());
        }

        Permanent bruiser = findPermanent(player1, "Junkblade Bruiser");
        assertThat(bruiser.getPowerModifier()).isEqualTo(2);
        assertThat(bruiser.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not get the expend boost before four total mana is spent")
    void doesNotGetBoostBelowExpendThreshold() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new JunkbladeBruiser());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        for (int i = 0; i < 3; i++) {
            harness.castAndResolveInstant(player1, 0, player2.getId());
        }

        Permanent bruiser = findPermanent(player1, "Junkblade Bruiser");
        assertThat(bruiser.getPowerModifier()).isZero();
        assertThat(bruiser.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The expend boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new JunkbladeBruiser());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 4);

        for (int i = 0; i < 4; i++) {
            harness.castAndResolveInstant(player1, 0, player2.getId());
        }

        Permanent bruiser = findPermanent(player1, "Junkblade Bruiser");
        assertThat(bruiser.getPowerModifier()).isEqualTo(2);
        assertThat(bruiser.getToughnessModifier()).isEqualTo(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(bruiser.getPowerModifier()).isZero();
        assertThat(bruiser.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Crossing four with one spell boosts only the existing Bruiser and only once")
    void crossingThresholdWithOneSpellTriggersOnlyOnce() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent bruiser = harness.addToBattlefieldAndReturn(player1, new JunkbladeBruiser());
        harness.setHand(player1, List.of(new JunkbladeBruiser(), new JunkbladeBruiser()));
        harness.addMana(player1, ManaColor.RED, 10);

        harness.castCreature(player1, 0);
        assertThat(bruiser.getPowerModifier()).isZero();
        assertThat(bruiser.getToughnessModifier()).isZero();
        harness.passBothPriorities();
        assertThat(bruiser.getPowerModifier()).isEqualTo(2);
        assertThat(bruiser.getToughnessModifier()).isEqualTo(1);
        harness.passBothPriorities();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(bruiser.getPowerModifier()).isEqualTo(2);
        assertThat(bruiser.getToughnessModifier()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent != bruiser)
                .allSatisfy(permanent -> {
                    assertThat(permanent.getPowerModifier()).isZero();
                    assertThat(permanent.getToughnessModifier()).isZero();
                });
    }

    @Test
    @DisplayName("Opponent spending four mana does not boost the Bruiser")
    void opponentsSpendingDoesNotTriggerBoost() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent bruiser = harness.addToBattlefieldAndReturn(player1, new JunkbladeBruiser());
        harness.setHand(player2, List.of(new JunkbladeBruiser()));
        harness.addMana(player2, ManaColor.RED, 5);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(bruiser.getPowerModifier()).isZero();
        assertThat(bruiser.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Bruiser entering after four mana was spent does not trigger retroactively")
    void enteringAfterThresholdDoesNotTriggerBoost() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new JunkbladeBruiser(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent bruiser = findPermanent(player1, "Junkblade Bruiser");
        assertThat(bruiser.getPowerModifier()).isZero();
        assertThat(bruiser.getToughnessModifier()).isZero();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(bruiser.getPowerModifier()).isZero();
        assertThat(bruiser.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Expend can trigger during the opponent's turn")
    void triggersDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent bruiser = harness.addToBattlefieldAndReturn(player1, new JunkbladeBruiser());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 4);

        for (int i = 0; i < 4; i++) {
            harness.castAndResolveInstant(player1, 0, player2.getId());
        }

        assertThat(bruiser.getPowerModifier()).isEqualTo(2);
        assertThat(bruiser.getToughnessModifier()).isEqualTo(1);
    }
}
