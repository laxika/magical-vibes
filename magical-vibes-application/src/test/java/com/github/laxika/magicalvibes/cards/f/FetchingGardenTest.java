package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.SakuraTribeScout;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FetchingGarden.class, SakuraTribeScout.class})
class FetchingGardenTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when played from hand")
    void entersTappedWhenPlayedFromHand() {
        harness.setHand(player1, List.of(new FetchingGarden()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Fetching Garden").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when put onto the battlefield from hand")
    void entersUntappedWhenPutOntoBattlefieldFromHand() {
        addCreatureReady(player1, new SakuraTribeScout());
        FetchingGarden garden = new FetchingGarden();
        harness.setHand(player1, List.of(garden));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Fetching Garden").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping produces only one mana, green or white")
    void producesOnlyOneMana() {
        harness.addToBattlefield(player1, new FetchingGarden());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)
                + gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Fetching Garden").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot produce mana immediately after being played from hand")
    void cannotProduceManaWhenPlayedTapped() {
        harness.setHand(player1, List.of(new FetchingGarden()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }
}
