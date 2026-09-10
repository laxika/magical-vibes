package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheMindStone.class, GrizzlyBears.class, Forest.class})
class TheMindStoneTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one white mana")
    void tapsForWhiteMana() {
        Permanent stone = harness.addToBattlefieldAndReturn(player1, new TheMindStone());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(stone.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not flicker a permanent before it is harnessed")
    void doesNotFlickerBeforeHarnessed() {
        harness.addToBattlefield(player1, new TheMindStone());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        UUID bearsId = bears.getId();

        advanceToEndStep();

        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isEqualTo(bearsId);
    }

    @Test
    @DisplayName("Harnessing flickers another nonland permanent at the end step")
    void harnessingFlickersAnotherPermanent() {
        Permanent stone = harness.addToBattlefieldAndReturn(player1, new TheMindStone());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        UUID bearsId = bears.getId();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(stone.isHarnessed()).isTrue();

        advanceToEndStep();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotEqualTo(bearsId);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target itself or a land")
    void rejectsInvalidTargets() {
        Permanent stone = harness.addToBattlefieldAndReturn(player1, new TheMindStone());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        advanceToEndStep();
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player1, "The Mind Stone")).isEqualTo(stone.getId());
        assertThat(harness.getPermanentId(player1, "Forest")).isEqualTo(forest.getId());
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
