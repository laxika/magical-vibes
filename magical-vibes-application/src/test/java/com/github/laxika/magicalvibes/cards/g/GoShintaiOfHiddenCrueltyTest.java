package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HondenOfSeeingWinds;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoShintaiOfHiddenCruelty.class, HondenOfSeeingWinds.class, GrizzlyBears.class, HillGiant.class})
class GoShintaiOfHiddenCrueltyTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1} destroys a target creature whose toughness is at most the Shrine count")
    void paysToDestroyCreatureWithinShrineCount() {
        harness.addToBattlefield(player1, new GoShintaiOfHiddenCruelty());
        harness.addToBattlefield(player1, new HondenOfSeeingWinds());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent hillGiant = addCreatureReady(player2, new HillGiant());

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                        findPermanent(player1, "Go-Shintai of Hidden Cruelty").getId(), bears.getId())
                .doesNotContain(hillGiant.getId());
        harness.handlePermanentChosen(player1, bears.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Declining the payment does not destroy a creature")
    void declinesPayment() {
        harness.addToBattlefield(player1, new GoShintaiOfHiddenCruelty());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
    }

    @Test
    @DisplayName("A creature above the Shrine count is not a legal target")
    void creatureAboveShrineCountCannotBeTargeted() {
        harness.addToBattlefield(player1, new GoShintaiOfHiddenCruelty());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
