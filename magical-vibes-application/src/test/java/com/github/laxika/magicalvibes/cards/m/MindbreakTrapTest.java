package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AngelsMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MindbreakTrapTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles any number of targeted spells")
    void exilesTargetedSpells() {
        GrizzlyBears bears = new GrizzlyBears();
        AngelsMercy mercy = new AngelsMercy();
        harness.setHand(player1, List.of(bears, mercy, new MindbreakTrap()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        harness.castInstant(player1, 0);
        harness.castInstant(player1, 0, List.of(bears.getId(), mercy.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getId())
                .containsExactlyInAnyOrder(bears.getId(), mercy.getId());
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Mindbreak Trap");
    }

    @Test
    @DisplayName("Exiles only the chosen spells")
    void exilesOnlyChosenSpells() {
        GrizzlyBears bears = new GrizzlyBears();
        AngelsMercy chosen = new AngelsMercy();
        AngelsMercy unchosen = new AngelsMercy();
        harness.setHand(player1, List.of(bears, chosen, unchosen, new MindbreakTrap()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);
        harness.castInstant(player1, 0);
        harness.castInstant(player1, 0);
        harness.castInstant(player1, 0, List.of(bears.getId(), chosen.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getId())
                .containsExactlyInAnyOrder(bears.getId(), chosen.getId());
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(27);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getId())
                .doesNotContain(unchosen.getId());
    }

    @Test
    @DisplayName("Free alternate cost requires one opponent to cast three spells")
    void freeAlternateCostRequiresThreeSpellsFromOneOpponent() {
        harness.setHand(player1, List.of(new MindbreakTrap()));
        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player1, 0, null, List.of()))
                .isInstanceOf(IllegalStateException.class);

        AngelsMercy first = new AngelsMercy();
        AngelsMercy second = new AngelsMercy();
        AngelsMercy third = new AngelsMercy();
        harness.setHand(player2, List.of(first, second, third));
        harness.addMana(player2, ManaColor.WHITE, 9);
        harness.addMana(player2, ManaColor.COLORLESS, 6);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0);
        harness.castInstant(player2, 0);
        harness.castInstant(player2, 0);
        harness.passPriority(player2);

        harness.castInstantWithAlternateCost(player1, 0, null, List.of());

        assertThat(gd.getSpellsCastThisTurnCount(player2.getId())).isEqualTo(3);
        assertThat(gd.getSpellsCastThisTurnCount(player1.getId())).isEqualTo(1);
    }
}
