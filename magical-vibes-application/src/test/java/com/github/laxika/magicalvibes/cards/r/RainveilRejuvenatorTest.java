package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RainveilRejuvenatorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may mill three cards")
    void etbMayMillThreeCards() {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new RainveilRejuvenator(), new RainveilRejuvenator(), new RainveilRejuvenator()));

        harness.setHand(player1, List.of(new RainveilRejuvenator()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
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

        harness.setHand(player1, List.of(new RainveilRejuvenator()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSize);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardSize);
    }

    @Test
    @DisplayName("Tap ability produces green mana equal to power")
    void tapAbilityProducesGreenManaEqualToPower() {
        harness.addToBattlefield(player1, new RainveilRejuvenator());
        var rejuvenator = gd.playerBattlefields.get(player1.getId()).getFirst();
        rejuvenator.setSummoningSick(false);
        rejuvenator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(4);
    }
}
