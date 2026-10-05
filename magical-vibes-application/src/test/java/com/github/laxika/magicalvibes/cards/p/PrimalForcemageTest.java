package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.s.StonewoodInvocation;
import com.github.laxika.magicalvibes.cards.s.SuddenShock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrimalForcemage.class, AshcoatBear.class, StonewoodInvocation.class, SuddenShock.class})
class PrimalForcemageTest extends BaseCardTest {

    @Test
    @DisplayName("Another creature you control entering gets +3/+3")
    void boostsEnteringCreature() {
        harness.addToBattlefield(player1, new PrimalForcemage());

        harness.castFromHand(player1, new AshcoatBear(), "{1}{G}");
        resolveAllTriggers();

        Permanent bears = findPermanent(player1, "Ashcoat Bear");
        assertThat(bears.getPowerModifier()).isEqualTo(3);
        assertThat(bears.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new PrimalForcemage());

        harness.castFromHand(player1, new AshcoatBear(), "{1}{G}");
        resolveAllTriggers();

        Permanent bears = findPermanent(player1, "Ashcoat Bear");
        assertThat(bears.getPowerModifier()).isEqualTo(3);
        assertThat(bears.getToughnessModifier()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's creature")
    void noTriggerForOpponentCreature() {
        harness.addToBattlefield(player1, new PrimalForcemage());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new AshcoatBear(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent bear = findPermanent(player2, "Ashcoat Bear");
        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not trigger for itself entering")
    void noTriggerForItself() {
        harness.castFromHand(player1, new PrimalForcemage(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent forcemage = findPermanent(player1, "Primal Forcemage");
        assertThat(forcemage.getPowerModifier()).isEqualTo(0);
        assertThat(forcemage.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not boost a creature already on the battlefield")
    void doesNotBoostCreatureAlreadyOnBattlefield() {
        harness.addToBattlefield(player1, new PrimalForcemage());
        Permanent existingBear = addCreatureReady(player1, new AshcoatBear());

        harness.castFromHand(player1, new AshcoatBear(), "{1}{G}");
        resolveAllTriggers();

        assertThat(existingBear.getPowerModifier()).isEqualTo(0);
        assertThat(existingBear.getToughnessModifier()).isEqualTo(0);

        Permanent enteringBear = findPermanents(player1, "Ashcoat Bear").get(1);
        assertThat(enteringBear.getPowerModifier()).isEqualTo(3);
        assertThat(enteringBear.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boosts the entering creature even if it gains shroud before resolution")
    void boostsEnteringCreatureWithShroud() {
        harness.addToBattlefield(player1, new PrimalForcemage());

        harness.castFromHand(player1, new AshcoatBear(), "{1}{G}");
        harness.passBothPriorities();

        Permanent enteringBear = findPermanent(player1, "Ashcoat Bear");
        assertThat(enteringBear.getPowerModifier()).isEqualTo(0);
        assertThat(enteringBear.getToughnessModifier()).isEqualTo(0);
        harness.setHand(player1, List.of(new StonewoodInvocation()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveInstant(player1, 0, enteringBear.getId());

        assertThat(enteringBear.getPowerModifier()).isEqualTo(5);
        assertThat(enteringBear.getToughnessModifier()).isEqualTo(5);
        resolveAllTriggers();

        assertThat(enteringBear.getPowerModifier()).isEqualTo(8);
        assertThat(enteringBear.getToughnessModifier()).isEqualTo(8);
    }

    @Test
    @DisplayName("Multiple Forcemages each boost the entering creature")
    void multipleForcemagesStackTheirBoosts() {
        harness.addToBattlefield(player1, new PrimalForcemage());
        harness.addToBattlefield(player1, new PrimalForcemage());

        harness.castFromHand(player1, new AshcoatBear(), "{1}{G}");
        resolveAllTriggers();

        Permanent bear = findPermanent(player1, "Ashcoat Bear");
        assertThat(bear.getPowerModifier()).isEqualTo(6);
        assertThat(bear.getToughnessModifier()).isEqualTo(6);
    }

    @Test
    @DisplayName("The trigger resolves even after Forcemage dies")
    void boostResolvesAfterSourceDies() {
        Permanent forcemage = harness.addToBattlefieldAndReturn(player1, new PrimalForcemage());
        harness.castFromHand(player1, new AshcoatBear(), "{1}{G}");
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new SuddenShock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, forcemage.getId());
        assertThat(findPermanents(player1, "Primal Forcemage")).isEmpty();
        resolveAllTriggers();

        Permanent bear = findPermanent(player1, "Ashcoat Bear");
        assertThat(bear.getPowerModifier()).isEqualTo(3);
        assertThat(bear.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("An entering creature can die before the boost resolves")
    void enteringCreatureDiesBeforeBoostResolves() {
        Permanent forcemage = harness.addToBattlefieldAndReturn(player1, new PrimalForcemage());
        harness.castFromHand(player1, new AshcoatBear(), "{1}{G}");
        harness.passBothPriorities();
        Permanent bear = findPermanent(player1, "Ashcoat Bear");

        harness.setHand(player1, List.of(new SuddenShock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, bear.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Ashcoat Bear")).isEmpty();
        assertThat(forcemage.getPowerModifier()).isEqualTo(0);
        assertThat(forcemage.getToughnessModifier()).isEqualTo(0);
        assertThat(gd.stack).isEmpty();
    }

}
