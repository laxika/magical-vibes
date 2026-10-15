package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SongOfTheDryads;
import com.github.laxika.magicalvibes.cards.s.SparringDummy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BendersWaterskin.class, SparringDummy.class, SongOfTheDryads.class})
class BendersWaterskinTest extends BaseCardTest {

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Tapping Bender's Waterskin adds one mana of the chosen color")
    void tapsForAnyColor(ManaColor color) {
        Permanent waterskin = harness.addToBattlefieldAndReturn(player1, new BendersWaterskin());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(waterskin.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Bender's Waterskin untaps itself during another player's untap step")
    void untapsItselfDuringOtherPlayersUntapStep() {
        Permanent waterskin = harness.addToBattlefieldAndReturn(player1, new BendersWaterskin());
        waterskin.tap();

        harness.performUntapStep(player2);

        assertThat(waterskin.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Bender's Waterskin does not untap other artifacts it controls")
    void doesNotUntapOtherArtifacts() {
        Permanent waterskin = harness.addToBattlefieldAndReturn(player1, new BendersWaterskin());
        Permanent otherArtifact = harness.addToBattlefieldAndReturn(player1, new SparringDummy());
        waterskin.tap();
        otherArtifact.tap();

        harness.performUntapStep(player2);

        assertThat(waterskin.isTapped()).isFalse();
        assertThat(otherArtifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Waterskin enchanted by Song of the Dryads stays tapped during an opponent's untap step")
    void doesNotUntapAfterLosingPrintedAbilities() {
        Permanent waterskin = harness.addToBattlefieldAndReturn(player1, new BendersWaterskin());
        harness.setHand(player1, List.of(new SongOfTheDryads()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, waterskin.getId());
        harness.passBothPriorities();
        waterskin.tap();

        harness.performUntapStep(player2);

        assertThat(waterskin.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Waterskin untaps normally during its controller's untap step")
    void untapsDuringOwnUntapStep() {
        Permanent waterskin = harness.addToBattlefieldAndReturn(player1, new BendersWaterskin());
        waterskin.tap();

        harness.performUntapStep(player1);

        assertThat(waterskin.isTapped()).isFalse();
    }
}
