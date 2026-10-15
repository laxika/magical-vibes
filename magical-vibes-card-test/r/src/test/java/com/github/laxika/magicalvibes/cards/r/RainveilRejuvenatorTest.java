package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(RainveilRejuvenator.class)
class RainveilRejuvenatorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may mill three cards")
    void etbMayMillThreeCards() {
        harness.setLibrary(player1, List.of(new RainveilRejuvenator(), new RainveilRejuvenator(), new RainveilRejuvenator()));

        harness.castFromHand(player1, new RainveilRejuvenator(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Declining the ETB may does not mill")
    void decliningEtbMayDoesNotMill() {
        int deckSize = gd.playerDecks.get(player1.getId()).size();
        int graveyardSize = gd.playerGraveyards.get(player1.getId()).size();

        harness.castFromHand(player1, new RainveilRejuvenator(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSize);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardSize);
    }

    @Test
    @DisplayName("Tap ability produces green mana equal to power")
    void tapAbilityProducesGreenManaEqualToPower() {
        var rejuvenator = harness.addToBattlefieldAndReturn(player1, new RainveilRejuvenator());
        rejuvenator.setSummoningSick(false);
        rejuvenator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(4);
        assertThat(rejuvenator.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void millsOnlyAvailableCardsFromShortLibrary() {
        var remaining = new RainveilRejuvenator();
        harness.setLibrary(player1, List.of(remaining));
        int opponentLibrarySize = gd.playerDecks.get(player2.getId()).size();
        harness.castFromHand(player1, new RainveilRejuvenator(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentLibrarySize);
    }

    @Test
    void zeroPowerProducesNoManaButStillTaps() {
        var rejuvenator = harness.addToBattlefieldAndReturn(player1, new RainveilRejuvenator());
        rejuvenator.setSummoningSick(false);
        rejuvenator.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(rejuvenator.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void negativePowerProducesNoMana() {
        var rejuvenator = harness.addToBattlefieldAndReturn(player1, new RainveilRejuvenator());
        rejuvenator.setSummoningSick(false);
        rejuvenator.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(rejuvenator.isTapped()).isTrue();
    }

    @Test
    void summoningSicknessPreventsActivation() {
        var rejuvenator = harness.addToBattlefieldAndReturn(player1, new RainveilRejuvenator());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(rejuvenator.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void cannotActivateAgainWhileTapped() {
        var rejuvenator = harness.addToBattlefieldAndReturn(player1, new RainveilRejuvenator());
        rejuvenator.setSummoningSick(false);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }
}
