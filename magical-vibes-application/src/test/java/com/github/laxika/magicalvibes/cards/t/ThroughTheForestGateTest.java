package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThroughTheForestGate.class, Forest.class, Island.class, GrizzlyBears.class})
class ThroughTheForestGateTest extends BaseCardTest {

    @Test
    @DisplayName("Puts any number of looked-at lands onto the battlefield tapped and gains 8 life")
    void putsLandsOntoBattlefieldTappedAndGainsLife() {
        Card forest = new Forest();
        Card island = new Island();
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, bears, island));
        castThroughTheForestGate();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(forest.getId(), island.getId());

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId(), island.getId()));

        List<Permanent> enteredLands = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == forest || permanent.getCard() == island)
                .toList();
        assertThat(enteredLands).hasSize(2).allMatch(Permanent::isTapped);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
        harness.assertLife(player1, 28);
    }

    @Test
    @DisplayName("With no land cards among the top cards, the library is shuffled and life is still gained")
    void noLandsStillGainsLife() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        castThroughTheForestGate();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        harness.assertLife(player1, 28);
    }

    private void castThroughTheForestGate() {
        harness.setHand(player1, List.of(new ThroughTheForestGate()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
