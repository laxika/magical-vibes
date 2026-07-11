package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeiAssassinsTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys the target opponent's only creature automatically")
    void etbDestroysOnlyCreatureAutomatically() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castWeiAssassins(player2.getId());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("ETB prompts target opponent to choose which creature to destroy")
    void etbPromptsOpponentToChoose() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        castWeiAssassins(player2.getId());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.DestroyChosenCreature.class);

        harness.handlePermanentChosen(player2, bears.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Giant Spider"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("ETB does nothing when target opponent controls no creatures")
    void etbDoesNothingWithNoCreatures() {
        castWeiAssassins(player2.getId());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no creatures to destroy"));
    }

    @Test
    @DisplayName("Cannot cast by targeting yourself")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new WeiAssassins()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.getGameService().playCard(gd, player1, 0, 0, player1.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    private void castWeiAssassins(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new WeiAssassins()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.getGameService().playCard(gd, player1, 0, 0, targetPlayerId, null);
    }
}
