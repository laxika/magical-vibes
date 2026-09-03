package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LapisLazuliTalisman.class, BalduvianBears.class, ZuranSpellcaster.class})
class LapisLazuliTalismanTest extends BaseCardTest {

    @Test
    @DisplayName("Controller casts a blue spell, pays {3}, untaps target permanent")
    void blueSpellPayUntapsTarget() {
        harness.addToBattlefield(player1, new LapisLazuliTalisman());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        bears.tap();

        harness.castFromHand(player1, new ZuranSpellcaster(), "{2}{U}");
        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds()).contains(bears.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the payment leaves the permanent tapped")
    void declineLeavesTapped() {
        harness.addToBattlefield(player1, new LapisLazuliTalisman());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        bears.tap();

        harness.castFromHand(player1, new ZuranSpellcaster(), "{2}{U}");
        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds()).contains(bears.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's blue spell triggers the Talisman's controller")
    void opponentBlueSpellTriggersController() {
        harness.addToBattlefield(player1, new LapisLazuliTalisman());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        bears.tap();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new ZuranSpellcaster(), "{2}{U}");

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.playerId()).isEqualTo(player1.getId());
        assertThat(targetChoice.validIds()).contains(bears.getId());

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A nonblue spell does not trigger the Talisman")
    void nonBlueSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new LapisLazuliTalisman());
        harness.castFromHand(player1, new BalduvianBears(), "{1}{G}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
