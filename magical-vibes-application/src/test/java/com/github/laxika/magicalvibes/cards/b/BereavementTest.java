package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BereavementTest extends BaseCardTest {

    @Test
    @DisplayName("When an opponent's green creature dies, that opponent discards a card")
    void opponentsGreenCreatureDiesOpponentDiscards() {
        harness.addToBattlefield(player1, new Bereavement());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, new ArrayList<>(List.of(new Forest())));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, new ArrayList<>(List.of(new CruelEdict())));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve Cruel Edict → player2 sacrifices Grizzly Bears
        harness.passBothPriorities(); // resolve Bereavement trigger → discard choice

        // The DYING creature's controller (player2), not Bereavement's controller, discards.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(c -> c instanceof Forest);
    }

    @Test
    @DisplayName("When Bereavement's controller's own green creature dies, that controller discards")
    void ownGreenCreatureDiesControllerDiscards() {
        harness.addToBattlefield(player1, new Bereavement());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, new ArrayList<>(List.of(new Forest())));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, new ArrayList<>(List.of(new CruelEdict())));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A non-green creature dying does not trigger Bereavement")
    void nonGreenCreatureDiesNoDiscard() {
        harness.addToBattlefield(player1, new Bereavement());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player2, new ArrayList<>(List.of(new Forest())));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, new ArrayList<>(List.of(new CruelEdict())));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }
}
