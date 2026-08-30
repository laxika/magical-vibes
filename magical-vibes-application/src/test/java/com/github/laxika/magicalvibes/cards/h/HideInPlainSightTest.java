package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HideInPlainSight.class, Forest.class, GrizzlyBears.class, Island.class,
        LlanowarElves.class, Shock.class})
class HideInPlainSightTest extends BaseCardTest {

    @Test
    void cloaksExactlyTwoOfTheTopFiveAndBottomsTheRestRandomly() {
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        Card shock = new Shock();
        Card island = new Island();
        Card elves = new LlanowarElves();
        harness.setLibrary(player1, List.of(bears, forest, shock, island, elves));
        harness.setHand(player1, List.of(new HideInPlainSight()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.minCount()).isEqualTo(2);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.selectedToBattlefieldCloaked()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), forest.getId()));

        List<Permanent> cloaked = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isCloaked)
                .toList();
        assertThat(cloaked).hasSize(2);
        assertThat(cloaked).extracting(Permanent::getCard)
                .containsExactlyInAnyOrder(bears, forest);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(shock, island, elves);
    }
}
