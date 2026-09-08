package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChartoothCougar.class, Mountain.class, Forest.class})
class ChartoothCougarTest extends BaseCardTest {

    @Test
    @DisplayName("Red ability gives Chartooth Cougar +1/+0 until end of turn")
    void redAbilityBoostsSelf() {
        Permanent cougar = addCreatureReady(player1, new ChartoothCougar());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(cougar.getPowerModifier()).isEqualTo(1);
        assertThat(cougar.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Red ability wears off at end of turn")
    void redAbilityWearsOffAtEndOfTurn() {
        Permanent cougar = addCreatureReady(player1, new ChartoothCougar());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(cougar.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(cougar.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Mountaincycling discards Chartooth Cougar and offers only Mountain cards")
    void mountaincyclingSearchesForMountain() {
        harness.setHand(player1, List.of(new ChartoothCougar()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new Mountain(), new Forest(), new Mountain()));

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Chartooth Cougar");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(card -> card instanceof Mountain)
                .hasSize(2);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Mountain");
    }
}
