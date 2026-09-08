package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BalduvianBarbarians;
import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HematiteTalisman.class, BalduvianBarbarians.class, BalduvianBears.class})
class HematiteTalismanTest extends BaseCardTest {

    @Test
    @DisplayName("Controller casts a red spell, pays {3}, untaps target permanent")
    void redSpellPayUntapsTarget() {
        harness.addToBattlefield(player1, new HematiteTalisman());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        bears.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFromHand(player1, new BalduvianBarbarians(), "{1}{R}{R}");
        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds()).contains(bears.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the payment leaves the permanent tapped")
    void declineLeavesTapped() {
        harness.addToBattlefield(player1, new HematiteTalisman());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        bears.tap();

        harness.castFromHand(player1, new BalduvianBarbarians(), "{1}{R}{R}");
        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds()).contains(bears.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's red spell triggers the Talisman's controller")
    void opponentRedSpellTriggersController() {
        harness.addToBattlefield(player1, new HematiteTalisman());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        bears.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new BalduvianBarbarians(), "{1}{R}{R}");

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.playerId())
                .isEqualTo(player1.getId());
        assertThat(targetChoice.validIds()).contains(bears.getId());

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A nonred spell does not trigger the Talisman")
    void nonRedSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new HematiteTalisman());
        harness.castFromHand(player1, new BalduvianBears(), "{1}{G}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
