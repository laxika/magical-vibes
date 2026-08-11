package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StormscapeMasterTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability grants chosen-color protection to a target creature until end of turn")
    void grantsChosenProtectionUntilEndOfTurn() {
        addCreatureReady(player1, new StormscapeMaster());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardColor.RED.name());

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.BLUE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("The second ability makes a target player lose 2 life and its controller gain 2 life")
    void drainsTargetPlayer() {
        addCreatureReady(player1, new StormscapeMaster());
        harness.addMana(player1, ManaColor.BLACK, 2);
        int controllerLife = gd.playerLifeTotals.get(player1.getId());
        int targetLife = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLife + 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(targetLife - 2);
    }

    @Test
    @DisplayName("The protection ability cannot target a noncreature permanent")
    void protectionAbilityRequiresCreatureTarget() {
        addCreatureReady(player1, new StormscapeMaster());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                0,
                0,
                null,
                harness.getPermanentId(player2, "Forest")
        )).isInstanceOf(IllegalStateException.class);
    }
}
