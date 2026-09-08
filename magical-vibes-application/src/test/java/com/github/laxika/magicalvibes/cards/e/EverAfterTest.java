package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EverAfterTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to two creatures as black Zombies")
    void returnsUpToTwoCreaturesAsBlackZombies() {
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        Card everAfter = new EverAfter();
        harness.setGraveyard(player1, List.of(bears, elves));
        harness.setHand(player1, List.of(everAfter));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, 0);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(bears.getId(), elves.getId());
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), elves.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getId().equals(bears.getId()) || p.getCard().getId().equals(elves.getId()))
                .hasSize(2)
                .allSatisfy(permanent -> {
                    assertThat(permanent.getGrantedColors()).contains(CardColor.BLACK);
                    assertThat(permanent.getGrantedSubtypes()).contains(CardSubtype.ZOMBIE);
                });
    }

    @Test
    @DisplayName("Puts itself on the bottom of its owner's library")
    void putsItselfOnBottomOfOwnersLibrary() {
        Card bears = new GrizzlyBears();
        Card everAfter = new EverAfter();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(everAfter));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getId().equals(bears.getId()))
                .singleElement()
                .satisfies(permanent -> {
                    assertThat(permanent.getGrantedColors()).contains(CardColor.BLACK);
                    assertThat(permanent.getGrantedSubtypes()).contains(CardSubtype.ZOMBIE);
                });
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(everAfter);
        assertThat(gd.playerDecks.get(player1.getId())).endsWith(everAfter);
    }
}
