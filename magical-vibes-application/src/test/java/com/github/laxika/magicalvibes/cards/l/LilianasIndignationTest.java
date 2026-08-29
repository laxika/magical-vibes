package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Liliana's Indignation")
class LilianasIndignationTest extends BaseCardTest {

    @Test
    @DisplayName("Mills X cards from its controller's library and counts creature cards")
    void millsControllerLibraryAndCountsCreatures() {
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new Forest(), new GrizzlyBears(), new Forest()));
        harness.setHand(player1, List.of(new LilianasIndignation()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int targetLifeBefore = gd.getLife(player2.getId());
        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).isNotEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.hasType(CardType.CREATURE))
                .hasSize(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(targetLifeBefore - 4);
    }

    @Test
    @DisplayName("Does not make the target lose life when no creature card was milled")
    void noCreatureCardsCauseNoLifeLoss() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new LilianasIndignation()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int targetLifeBefore = gd.getLife(player2.getId());
        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(targetLifeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Rejects a non-player target")
    void rejectsNonPlayerTarget() {
        harness.setHand(player1, List.of(new LilianasIndignation()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        var target = gd.playerBattlefields.get(player2.getId()).getFirst().getId();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, target))
                .isInstanceOf(IllegalStateException.class);
    }
}
