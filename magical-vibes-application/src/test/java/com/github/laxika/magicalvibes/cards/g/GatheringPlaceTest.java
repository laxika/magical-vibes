package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GatheringPlace.class, Forest.class})
class GatheringPlaceTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Gathering Place produces colorless mana")
    void tappingProducesColorlessMana() {
        Permanent land = addReadyGatheringPlace();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Green or white mana requires a newly entered Gathering Place or a basic land")
    void coloredManaRequiresCondition() {
        Permanent land = addReadyGatheringPlace();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("entered this turn or if you control a basic land");
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A Gathering Place that entered this turn can produce green or white mana")
    void newlyEnteredGatheringPlaceProducesChosenMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new GatheringPlace());
        gd.permanentsEnteredBattlefieldThisTurn.put(
                player1.getId(), new ArrayList<>(List.of(land.getCard())));

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A basic land enables Gathering Place to produce green or white mana")
    void basicLandEnablesChosenMana() {
        Permanent land = addReadyGatheringPlace();
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(land.isTapped()).isTrue();
    }

    private Permanent addReadyGatheringPlace() {
        Permanent land = new Permanent(new GatheringPlace());
        land.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(land);
        return land;
    }
}
