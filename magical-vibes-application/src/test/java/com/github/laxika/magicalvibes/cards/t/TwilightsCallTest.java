package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TwilightsCallTest extends BaseCardTest {

    @Test
    @DisplayName("Returns all creature cards from each player's graveyard")
    void returnsCreaturesFromEachGraveyard() {
        Card player1Creature = new GrizzlyBears();
        Card land = new Island();
        Card player2Creature = new RagingGoblin();
        harness.setGraveyard(player1, new ArrayList<>(List.of(player1Creature, land)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(player2Creature)));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new TwilightsCall()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Raging Goblin");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(player1Creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(player2Creature);
    }

    @Test
    @DisplayName("Can be cast as though it had flash by paying two more")
    void flashCastPaysTwoMore() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        TwilightsCall spell = new TwilightsCall();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("Cannot be cast for its normal cost on an opponent's turn")
    void normalCostDoesNotGrantFlash() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new TwilightsCall()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
