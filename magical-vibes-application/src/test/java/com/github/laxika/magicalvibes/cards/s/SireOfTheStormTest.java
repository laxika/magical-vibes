package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DesperateRitual;
import com.github.laxika.magicalvibes.cards.d.DevotedRetainer;
import com.github.laxika.magicalvibes.cards.h.HarshDeceiver;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SireOfTheStorm.class, DesperateRitual.class, HarshDeceiver.class, DevotedRetainer.class})
class SireOfTheStormTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an Arcane spell lets the controller draw a card")
    void arcaneSpellDrawsCard() {
        harness.addToBattlefield(player1, new SireOfTheStorm());
        harness.castFromHand(player1, new DesperateRitual(), "{1}{R}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Casting a Spirit spell lets the controller draw a card")
    void spiritSpellDrawsCard() {
        harness.addToBattlefield(player1, new SireOfTheStorm());
        harness.castFromHand(player1, new HarshDeceiver(), "{3}{W}");

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining the trigger draws nothing")
    void decliningDrawsNothing() {
        harness.addToBattlefield(player1, new SireOfTheStorm());
        harness.castFromHand(player1, new DesperateRitual(), "{1}{R}");
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Casting a non-Spirit non-Arcane spell does not trigger")
    void unrelatedSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new SireOfTheStorm());
        harness.castFromHand(player1, new DevotedRetainer(), "{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Casting a matching spell by an opponent does not trigger")
    void opponentSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new SireOfTheStorm());
        harness.setHand(player1, List.of());
        harness.castFromHand(player2, new DesperateRitual(), "{1}{R}");

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
