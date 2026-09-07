package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AmbrosiaWhiteheart.class, Island.class})
class AmbrosiaWhiteheartTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may return another permanent you control")
    void etbMayReturnAnotherPermanent() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());

        castAmbrosia();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(island.getId());

        harness.handlePermanentChosen(player1, island.getId());

        harness.assertInHand(player1, "Island");
        harness.assertOnBattlefield(player1, "Ambrosia Whiteheart");
    }

    @Test
    @DisplayName("Declining the ETB ability leaves permanents on the battlefield")
    void decliningEtbAbilityDoesNothing() {
        harness.addToBattlefield(player1, new Island());

        castAmbrosia();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Island");
        harness.assertOnBattlefield(player1, "Ambrosia Whiteheart");
    }

    @Test
    @DisplayName("Landfall gives Ambrosia Whiteheart +1/+0 until end of turn")
    void landfallBoostsAmbrosiaWhiteheart() {
        Permanent ambrosia = harness.addToBattlefieldAndReturn(player1, new AmbrosiaWhiteheart());
        harness.setHand(player1, List.of(new Island()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(ambrosia.getEffectivePower()).isEqualTo(3);
        assertThat(ambrosia.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ambrosia.getEffectivePower()).isEqualTo(2);
        assertThat(ambrosia.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's land does not trigger landfall")
    void opponentLandDoesNotTrigger() {
        Permanent ambrosia = harness.addToBattlefieldAndReturn(player1, new AmbrosiaWhiteheart());
        harness.setHand(player2, List.of(new Island()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(ambrosia.getEffectivePower()).isEqualTo(2);
        assertThat(ambrosia.getEffectiveToughness()).isEqualTo(2);
    }

    private void castAmbrosia() {
        harness.setHand(player1, List.of(new AmbrosiaWhiteheart()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
