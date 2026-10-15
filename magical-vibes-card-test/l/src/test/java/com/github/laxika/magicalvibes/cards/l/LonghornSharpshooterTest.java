package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AvenInterrupter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LonghornSharpshooter.class, AvenInterrupter.class})
class LonghornSharpshooterTest extends BaseCardTest {

    @Test
    void dealsTwoDamageWhenPlottedFromHand() {
        LonghornSharpshooter sharpshooter = new LonghornSharpshooter();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(sharpshooter));
        addPlotMana();

        harness.castWithAlternateCost(player1, 0, List.of());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void triggersWhenAnotherEffectPlotsIt() {
        LonghornSharpshooter sharpshooter = new LonghornSharpshooter();
        AvenInterrupter interrupter = new AvenInterrupter();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(interrupter));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castFromHand(player1, sharpshooter, "{2}{R}");
        harness.passPriority(player1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, sharpshooter.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void ordinaryCastingDoesNotDealDamage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new LonghornSharpshooter(), "{2}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Longhorn Sharpshooter");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    void plottedTriggerCanKillACreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvenInterrupter());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LonghornSharpshooter()));
        addPlotMana();

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Aven Interrupter");
        harness.assertInGraveyard(player2, "Aven Interrupter");
        harness.assertLife(player2, 20);
    }

    @Test
    void plottedCardCanBeCastForFreeOnlyOnALaterTurnWithoutDealingDamageAgain() {
        LonghornSharpshooter sharpshooter = new LonghornSharpshooter();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(sharpshooter));
        addPlotMana();
        harness.castWithAlternateCost(player1, 0, List.of());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Longhorn Sharpshooter");
        harness.assertNotOnBattlefield(player1, "Longhorn Sharpshooter");
        assertThatThrownBy(() -> harness.castFromExile(player1, sharpshooter.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.setHand(player2, List.of());
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, sharpshooter.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Longhorn Sharpshooter");
        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    void plottingIsUnavailableOutsideAMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new LonghornSharpshooter()));
        addPlotMana();

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Longhorn Sharpshooter");
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    private void addPlotMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
