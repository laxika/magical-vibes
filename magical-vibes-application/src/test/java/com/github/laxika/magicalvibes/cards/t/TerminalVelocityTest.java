package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TerminalVelocity.class, AirElemental.class, GrizzlyBears.class, Mountain.class, Ornithopter.class})
class TerminalVelocityTest extends BaseCardTest {

    @Test
    @DisplayName("Offers artifact and creature cards in hand")
    void offersArtifactsAndCreatures() {
        harness.setHand(player1, List.of(new TerminalVelocity(), new Mountain(), new GrizzlyBears(), new Ornithopter()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(1, 2);
    }

    @Test
    @DisplayName("The chosen permanent has haste, deals damage on leaving, and is sacrificed at end step")
    void chosenPermanentGetsAllGrantedAbilities() {
        harness.setHand(player1, List.of(new TerminalVelocity(), new AirElemental()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        Permanent elemental = findPermanent(player1, "Air Elemental");
        assertThat(elemental.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Air Elemental");
        harness.assertInGraveyard(player1, "Air Elemental");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining leaves the hand unchanged")
    void decliningLeavesCardInHand() {
        harness.setHand(player1, List.of(new TerminalVelocity(), new GrizzlyBears()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);
    }
}
