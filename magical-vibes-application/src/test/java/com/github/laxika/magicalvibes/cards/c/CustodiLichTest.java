package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CustodiLich.class, GrizzlyBears.class})
class CustodiLichTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes the monarch and makes a target player sacrifice a creature")
    void becomesMonarchAndSacrificesTargetPlayersCreature() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());

        harness.enterBattlefieldAndReturn(player1, new CustodiLich());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.playerId()).isEqualTo(player1.getId());
        assertThat(targetChoice.validPlayerIds()).containsExactlyInAnyOrder(player1.getId(), player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
