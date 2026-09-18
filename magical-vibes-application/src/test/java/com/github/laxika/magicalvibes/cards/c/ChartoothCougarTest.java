package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({ChartoothCougar.class, Mountain.class})
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
        harness.setLibrary(player1, List.of(new Mountain(), new ChartoothCougar(), new Mountain()));

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Chartooth Cougar");
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();
        assertThat(search.params().shuffleAfterSelection()).isTrue();
        assertThat(search.params().cards())
                .allMatch(card -> card instanceof Mountain)
                .hasSize(2);

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Mountain");
    }

    @Test
    @DisplayName("Mountaincycling can fail to find a Mountain")
    void mountaincyclingCanFailToFindMountain() {
        ChartoothCougar nonMountain = new ChartoothCougar();
        harness.setHand(player1, List.of(new ChartoothCougar()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(nonMountain));

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Chartooth Cougar");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonMountain);
    }
}
