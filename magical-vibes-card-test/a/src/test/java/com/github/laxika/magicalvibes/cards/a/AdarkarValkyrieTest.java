package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BorealDruid;
import com.github.laxika.magicalvibes.cards.s.Skred;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredMountain;
import com.github.laxika.magicalvibes.cards.s.SurgingAether;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AdarkarValkyrie.class, BorealDruid.class, Skred.class, SnowCoveredMountain.class, SurgingAether.class})
class AdarkarValkyrieTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a targeted creature under the Valkyrie's controller's control when it dies this turn")
    void returnsTargetedCreatureUnderAbilityControllersControl() {
        Permanent valkyrie = addCreatureReady(player1, new AdarkarValkyrie());
        harness.addToBattlefield(player1, new SnowCoveredMountain());
        harness.addToBattlefield(player2, new BorealDruid());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Boreal Druid"));
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Skred()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Boreal Druid"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Boreal Druid");
        harness.assertNotOnBattlefield(player2, "Boreal Druid");
        harness.assertNotInGraveyard(player2, "Boreal Druid");
        assertThat(findPermanent(player1, "Boreal Druid")).matches(permanent -> !permanent.isTapped());
        assertThat(valkyrie.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target the Valkyrie itself")
    void cannotTargetItself() {
        Permanent valkyrie = addCreatureReady(player1, new AdarkarValkyrie());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, valkyrie.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new AdarkarValkyrie());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SnowCoveredMountain());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not register the delayed return if the target leaves before the ability resolves")
    void doesNotRegisterReturnWhenTargetLeavesBeforeAbilityResolves() {
        addCreatureReady(player1, new AdarkarValkyrie());
        harness.addToBattlefield(player1, new SnowCoveredMountain());
        BorealDruid targetCard = new BorealDruid();
        targetCard.setOwnerId(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, target.getId());

        harness.setHand(player1, List.of(new Skred()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Boreal Druid");
        harness.assertInGraveyard(player2, "Boreal Druid");
    }

    @Test
    @DisplayName("Does not follow a creature that leaves and is recast before dying")
    void doesNotReturnRecastCreature() {
        addCreatureReady(player1, new AdarkarValkyrie());
        harness.addToBattlefield(player1, new SnowCoveredMountain());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BorealDruid());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new SurgingAether()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Boreal Druid");

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Skred()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Boreal Druid"));
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertNotOnBattlefield(player1, "Boreal Druid");
        harness.assertInGraveyard(player1, "Boreal Druid");
    }

    @Test
    @DisplayName("The delayed return still works after the Valkyrie dies")
    void returnsCreatureAfterValkyrieDies() {
        Permanent valkyrie = addCreatureReady(player1, new AdarkarValkyrie());
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new SnowCoveredMountain());
        }
        BorealDruid targetCard = new BorealDruid();
        targetCard.setOwnerId(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Skred(), new Skred()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, valkyrie.getId());
        harness.assertInGraveyard(player1, "Adarkar Valkyrie");
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Boreal Druid");
        harness.assertNotInGraveyard(player2, "Boreal Druid");
    }

    @Test
    @DisplayName("A delayed return only returns the creature once")
    void returnsCreatureOnlyOnce() {
        addCreatureReady(player1, new AdarkarValkyrie());
        BorealDruid targetCard = new BorealDruid();
        targetCard.setOwnerId(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Skred(), new Skred()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Boreal Druid"));

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Boreal Druid");
        harness.assertInGraveyard(player2, "Boreal Druid");
    }

    @Test
    @DisplayName("The delayed return expires at the end of the turn")
    void doesNotReturnCreatureOnNextTurn() {
        addCreatureReady(player1, new AdarkarValkyrie());
        BorealDruid targetCard = new BorealDruid();
        targetCard.setOwnerId(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player1, List.of(new Skred()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Boreal Druid");
        harness.assertInGraveyard(player2, "Boreal Druid");
    }
}
