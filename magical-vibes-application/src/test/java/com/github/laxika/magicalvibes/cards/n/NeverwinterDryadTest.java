package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NeverwinterDryad.class, Forest.class, SnowCoveredForest.class, Plains.class, GrizzlyBears.class})
class NeverwinterDryadTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and searches for a basic Forest onto the battlefield tapped")
    void sacrificesAndSearchesForBasicForest() {
        Permanent dryad = harness.addToBattlefieldAndReturn(player1, new NeverwinterDryad());
        Forest forest = new Forest();
        SnowCoveredForest snowCoveredForest = new SnowCoveredForest();
        Plains plains = new Plains();
        GrizzlyBears bears = new GrizzlyBears();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(forest, snowCoveredForest, plains, bears));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(dryad.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(dryad.getCard().getId());

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(forest.getId(), snowCoveredForest.getId());

        Card chosen = search.params().cards().getFirst();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(chosen.getId()) && permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrderElementsOf(List.of(forest, snowCoveredForest, plains, bears).stream()
                        .filter(card -> card != chosen).map(Card::getId).toList());
    }
}
