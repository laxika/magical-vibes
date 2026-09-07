package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.t.TempleGarden;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlourishingBloomKin.class, Forest.class, TempleGarden.class})
class FlourishingBloomKinTest extends BaseCardTest {

    @Test
    void getsPlusOnePlusOneForEachForestYouControl() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent bloomKin = harness.addToBattlefieldAndReturn(player1, new FlourishingBloomKin());

        assertThat(gqs.getEffectivePower(gd, bloomKin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bloomKin)).isEqualTo(2);
    }

    @Test
    void turningFaceUpSearchesForestCardsAndSplitsThemBetweenBattlefieldAndHand() {
        harness.addToBattlefield(player1, new Forest());
        Forest libraryForest = new Forest();
        TempleGarden libraryTempleGarden = new TempleGarden();
        harness.setLibrary(player1, List.of(libraryForest, libraryTempleGarden));
        harness.setHand(player1, List.of(new FlourishingBloomKin()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bloomKin = findPermanent(player1, "Flourishing Bloom-Kin");
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bloomKin));
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().cards()).containsExactly(libraryForest, libraryTempleGarden);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(libraryTempleGarden);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == libraryForest && permanent.isTapped());
        assertThat(gd.playerHands.get(player1.getId())).contains(libraryTempleGarden);
    }
}
