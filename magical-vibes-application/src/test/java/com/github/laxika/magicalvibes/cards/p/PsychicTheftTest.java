package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PsychicTheftTest extends BaseCardTest {

    @Test
    void choosesOnlyAnInstantOrSorceryFromTheRevealedHand() {
        Card land = new Swamp();
        Card creature = new GrizzlyBears();
        Card spell = new Divination();
        harness.setHand(player2, List.of(land, creature, spell));
        harness.setHand(player1, List.of(new PsychicTheft()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .hasMessageContaining("valid");
        harness.handleCardChosen(player1, 2);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(spell);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land, creature);
        assertThat(gd.exilePlayPermissions.get(spell.getId())).isEqualTo(player1.getId());
    }

    @Test
    void returnsTheUncastCardToItsOwnersHandAtTheNextEndStep() {
        Card spell = new Divination();
        harness.setHand(player2, List.of(spell));
        harness.setHand(player1, List.of(new PsychicTheft()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).contains(spell);
    }

    @Test
    void mayCastTheExiledCardBeforeTheDelayedReturn() {
        Card spell = new Divination();
        harness.setHand(player2, List.of(spell));
        harness.setHand(player1, List.of(new PsychicTheft()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.addMana(player1, ManaColor.BLUE, 3);
        gs.playCardFromExile(gd, player1, spell.getId(), null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(spell);
    }
}
