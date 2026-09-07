package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BeamsawProspector.class, Shock.class, Forest.class})
class BeamsawProspectorTest extends BaseCardTest {

    @Test
    @DisplayName("When Beamsaw Prospector dies, its controller creates a Lander token")
    void deathTriggerCreatesLanderToken() {
        Permanent prospector = harness.addToBattlefieldAndReturn(player1, new BeamsawProspector());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, prospector.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Beamsaw Prospector");
        assertThat(findPermanents(player1, "Lander")).isEmpty();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Lander")).hasSize(1);
        assertThat(findPermanents(player2, "Lander")).isEmpty();
    }

    @Test
    @DisplayName("The Lander created by Beamsaw Prospector searches for a tapped basic land")
    void createdLanderSearchesForTappedBasicLand() {
        harness.addToBattlefield(player1, new BeamsawProspector());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0,
                harness.getGameData().playerBattlefields.get(player1.getId()).getFirst().getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        Permanent lander = findPermanents(player1, "Lander").getFirst();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                harness.getGameData().playerBattlefields.get(player1.getId()).indexOf(lander),
                0, null, null);
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibrarySearch.class);
    }
}
