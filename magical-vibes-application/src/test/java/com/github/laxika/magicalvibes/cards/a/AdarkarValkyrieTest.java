package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BorealDruid;
import com.github.laxika.magicalvibes.cards.s.Skred;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredMountain;
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

@CardUsed({AdarkarValkyrie.class, BorealDruid.class, Skred.class, SnowCoveredMountain.class})
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
        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Boreal Druid"));
        harness.passBothPriorities();
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
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BorealDruid());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, target.getId());

        harness.setHand(player1, List.of(new Skred()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Boreal Druid");
        harness.assertInGraveyard(player2, "Boreal Druid");
    }
}
