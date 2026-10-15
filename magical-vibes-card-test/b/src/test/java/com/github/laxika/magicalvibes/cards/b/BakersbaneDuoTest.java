package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BakersbaneDuo.class, Shock.class})
class BakersbaneDuoTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Food token when it enters")
    void createsFoodOnEnter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BakersbaneDuo()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bakersbane Duo");
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Gets +1/+1 when its controller expends four")
    void getsBoostWhenControllerExpendsFour() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BakersbaneDuo(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(findPermanent(player1, "Bakersbane Duo").getPowerModifier()).isEqualTo(1);
        assertThat(findPermanent(player1, "Bakersbane Duo").getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not get the expend boost before four total mana is spent")
    void doesNotGetBoostBelowExpendThreshold() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BakersbaneDuo(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(findPermanent(player1, "Bakersbane Duo").getPowerModifier()).isZero();
        assertThat(findPermanent(player1, "Bakersbane Duo").getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Food can be sacrificed for three life without counting as mana spent on spells")
    void foodActivationDoesNotCountTowardsExpend() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new BakersbaneDuo()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);

        harness.activateAbility(player1, 1, null, null);
        harness.assertNotOnBattlefield(player1, "Food");
        harness.assertLife(player1, 10);
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        assertThat(findPermanent(player1, "Bakersbane Duo").getPowerModifier()).isZero();
        assertThat(findPermanent(player1, "Bakersbane Duo").getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Expend triggers once per turn and its boost expires at end of turn")
    void triggersOnlyOnceAndBoostExpires() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        var duo = harness.addToBattlefieldAndReturn(player1, new BakersbaneDuo());
        harness.setHand(player1, List.of(new BakersbaneDuo(), new BakersbaneDuo(), new BakersbaneDuo()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        for (int i = 0; i < 3; i++) {
            harness.castCreature(player1, 0);
            harness.passBothPriorities();
            harness.passBothPriorities();
            if (i == 1) {
                harness.passBothPriorities();
                harness.passBothPriorities();
            }
            assertThat(duo.getPowerModifier()).isEqualTo(i == 0 ? 0 : 1);
            assertThat(duo.getToughnessModifier()).isEqualTo(i == 0 ? 0 : 1);
        }

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(duo.getPowerModifier()).isZero();
        assertThat(duo.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("An opponent expending four does not boost Bakersbane Duo")
    void opponentsManaSpendingDoesNotTrigger() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        var duo = harness.addToBattlefieldAndReturn(player1, new BakersbaneDuo());
        harness.setHand(player2, List.of(new BakersbaneDuo(), new BakersbaneDuo()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        for (int i = 0; i < 2; i++) {
            harness.castCreature(player2, 0);
            harness.passBothPriorities();
            harness.passBothPriorities();
            if (i == 1) {
                harness.passBothPriorities();
            }
        }

        assertThat(duo.getPowerModifier()).isZero();
        assertThat(duo.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Bakersbane Duo does not trigger for mana spent casting itself")
    void doesNotTriggerForItsOwnCastingCost() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BakersbaneDuo(), new BakersbaneDuo()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        var first = findPermanent(player1, "Bakersbane Duo");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getPowerModifier()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Bakersbane Duo") && p != first)
                .singleElement()
                .satisfies(p -> {
                    assertThat(p.getPowerModifier()).isZero();
                    assertThat(p.getToughnessModifier()).isZero();
                });
    }
}
