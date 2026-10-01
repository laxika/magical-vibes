package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GhostWarden;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CerebralVortex.class, GhostWarden.class})
class CerebralVortexTest extends BaseCardTest {

    @Test
    @DisplayName("Target player draws two cards, then takes damage equal to cards drawn this turn")
    void drawsThenDealsDamageBasedOnCardsDrawnThisTurn() {
        harness.setHand(player1, List.of(new CerebralVortex()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(
                new GhostWarden(), new GhostWarden(), new GhostWarden(), new GhostWarden()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = addCreatureReady(player2, new GhostWarden());
        harness.setHand(player1, List.of(new CerebralVortex()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
