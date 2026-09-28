package com.github.laxika.magicalvibes.cards.l;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({LadyLokiAgentOfChaos.class, CounselOfTheSoratami.class, Forest.class, GrizzlyBears.class})
class LadyLokiAgentOfChaosTest extends BaseCardTest {

    @Test
    @DisplayName("The first matching spell is exiled, deals the mana-value difference, and offers a free cast")
    void firstMatchingSpellExilesAndDealsDifference() {
        harness.addToBattlefield(player1, new LadyLokiAgentOfChaos());
        Forest forest = new Forest();
        GrizzlyBears hit = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, hit));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(forest, hit,
                gd.getPlayerExiledCards(player1.getId()).stream()
                        .filter(card -> card.getName().equals("Counsel of the Soratami"))
                        .findFirst().orElseThrow());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.stack).anyMatch(stackEntry -> stackEntry.getPhysicalCard() == hit);
    }

    @Test
    @DisplayName("Only the first instant, sorcery, or Villain spell each turn triggers")
    void onlyFirstMatchingSpellTriggers() {
        harness.addToBattlefield(player1, new LadyLokiAgentOfChaos());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new CounselOfTheSoratami(), new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.castSorcery(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }
}
