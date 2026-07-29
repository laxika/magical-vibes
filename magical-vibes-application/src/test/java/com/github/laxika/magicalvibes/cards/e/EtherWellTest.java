package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EtherWellTest extends BaseCardTest {

    @Test
    @DisplayName("A nonred creature goes on top of its owner's library with no choice offered")
    void nonredCreatureGoesOnTop() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castEtherWell(bears);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("A red creature offers the bottom-of-library choice; accepting puts it on the bottom")
    void redCreatureAcceptedGoesOnBottom() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        castEtherWell(giant);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId()).getLast().getName()).isEqualTo("Hill Giant");
    }

    @Test
    @DisplayName("Declining the red creature's choice leaves it on top of its owner's library")
    void redCreatureDeclinedGoesOnTop() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        castEtherWell(giant);

        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Hill Giant");
    }

    private void castEtherWell(Permanent target) {
        harness.setHand(player1, List.of(new EtherWell()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
