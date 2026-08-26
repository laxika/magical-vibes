package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoltenManInfernoIncarnate.class, Mountain.class, Forest.class, CruelEdict.class})
class MoltenManInfernoIncarnateTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by searching for a basic Mountain that enters tapped")
    void entersWithBasicMountainSearch() {
        harness.setHand(player1, List.of(new MoltenManInfernoIncarnate()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addToBattlefield(player1, new Mountain());
        setupLibrary(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(1);
        assertThat(search.params().cards().get(0)).isInstanceOf(Mountain.class);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof Mountain && permanent.isTapped());
    }

    @Test
    @DisplayName("Gets +1/+1 for each Mountain its controller controls")
    void getsPlusOnePlusOneForEachMountain() {
        harness.addToBattlefield(player1, new Mountain());
        Permanent moltenMan = addCreatureReady(player1, new MoltenManInfernoIncarnate());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Mountain());

        assertThat(gqs.getEffectivePower(gd, moltenMan)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, moltenMan)).isEqualTo(1);

        harness.addToBattlefield(player1, new Mountain());

        assertThat(gqs.getEffectivePower(gd, moltenMan)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, moltenMan)).isEqualTo(2);
    }

    @Test
    @DisplayName("When it leaves, its controller sacrifices a land")
    void leavesAndItsControllerSacrificesALand() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new MoltenManInfernoIncarnate());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof Forest)
                .findFirst()
                .orElseThrow();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, new ArrayList<>(List.of(new CruelEdict())));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof Forest)
                .anyMatch(permanent -> permanent.getCard() instanceof Mountain)
                .noneMatch(permanent -> permanent.getCard() instanceof MoltenManInfernoIncarnate);
    }

    private void setupLibrary(com.github.laxika.magicalvibes.model.Player player) {
        List<Card> deck = gd.playerDecks.get(player.getId());
        deck.clear();
        deck.addAll(List.of(new Forest(), new Mountain()));
    }
}
