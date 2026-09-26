package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CoastalTower;
import com.github.laxika.magicalvibes.cards.r.RagingKavu;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StormscapeMaster.class, RagingKavu.class, CoastalTower.class})
class StormscapeMasterTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability grants chosen-color protection to a target creature until end of turn")
    void grantsChosenProtectionUntilEndOfTurn() {
        Permanent source = addCreatureReady(player1, new StormscapeMaster());
        Permanent target = addCreatureReady(player2, new RagingKavu());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardColor.RED.name());

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.BLUE)).isFalse();
        assertThat(source.isTapped()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("The second ability makes a target player lose 2 life and its controller gain 2 life")
    void drainsTargetPlayer() {
        Permanent source = addCreatureReady(player1, new StormscapeMaster());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The second ability can target its controller")
    void drainsItsController() {
        Permanent source = addCreatureReady(player1, new StormscapeMaster());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The protection ability cannot target a noncreature permanent")
    void protectionAbilityRequiresCreatureTarget() {
        addCreatureReady(player1, new StormscapeMaster());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CoastalTower());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                0,
                0,
                null,
                target.getId()
        )).isInstanceOf(IllegalStateException.class);
    }
}
