package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DredgeTheMire.class, GrizzlyBears.class, Island.class})
class DredgeTheMireTest extends BaseCardTest {

    @Test
    void opponentChoosesCreatureFromTheirGraveyardAndItEntersUnderYourControl() {
        Card opponentCreature = new GrizzlyBears();
        Card opponentLand = new Island();
        Card controllerCreature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(opponentLand, opponentCreature));
        harness.setGraveyard(player1, List.of(controllerCreature));
        harness.setHand(player1, List.of(new DredgeTheMire()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.cardPool()).containsExactly(opponentCreature);

        harness.handleGraveyardCardChosen(player2, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).containsExactly(opponentCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(controllerCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentLand);
    }

    @Test
    void ignoresCreatureCardsInControllerGraveyardAndNoncreaturesInOpponentGraveyard() {
        Card controllerCreature = new GrizzlyBears();
        Card opponentLand = new Island();
        harness.setGraveyard(player1, List.of(controllerCreature));
        harness.setGraveyard(player2, List.of(opponentLand));
        harness.setHand(player1, List.of(new DredgeTheMire()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(controllerCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentLand);
    }
}
