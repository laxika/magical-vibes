package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PatriarchsBidding.class, GrizzlyBears.class, HillGiant.class, AvianChangeling.class})
class PatriarchsBiddingTest extends BaseCardTest {

    @Test
    @DisplayName("Each player chooses a type and returns matching creatures from their graveyard")
    void eachPlayerChoosesTheirOwnType() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new HillGiant()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new HillGiant()));
        cast();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleListChoice(player1, "BEAR");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleListChoice(player2, "GIANT");

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Changeling creature cards match each player's chosen type")
    void changelingMatchesChosenType() {
        harness.setGraveyard(player1, List.of(new AvianChangeling()));
        harness.setGraveyard(player2, List.of(new AvianChangeling()));
        cast();

        harness.handleListChoice(player1, "BEAR");
        harness.handleListChoice(player2, "GIANT");

        assertThat(findPermanents(player1, "Avian Changeling")).hasSize(1);
        assertThat(findPermanents(player2, "Avian Changeling")).hasSize(1);
    }

    private void cast() {
        harness.setHand(player1, List.of(new PatriarchsBidding()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
