package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MerfolkOfThePearlTrident;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LapisLazuliTalismanTest extends BaseCardTest {

    @Test
    @DisplayName("Controller casts a blue spell, pays {3}, untaps target permanent")
    void blueSpellPayUntapsTarget() {
        harness.addToBattlefield(player1, new LapisLazuliTalisman());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();
        harness.setHand(player1, List.of(new MerfolkOfThePearlTrident()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the payment leaves the permanent tapped")
    void declineLeavesTapped() {
        harness.addToBattlefield(player1, new LapisLazuliTalisman());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();
        harness.setHand(player1, List.of(new MerfolkOfThePearlTrident()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's blue spell triggers the Talisman's controller")
    void opponentBlueSpellTriggersController() {
        harness.addToBattlefield(player1, new LapisLazuliTalisman());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new MerfolkOfThePearlTrident()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player2, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A nonblue spell does not trigger the Talisman")
    void nonBlueSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new LapisLazuliTalisman());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
