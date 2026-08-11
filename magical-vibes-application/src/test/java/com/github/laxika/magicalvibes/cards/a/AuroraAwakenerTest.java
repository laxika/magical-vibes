package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AuroraAwakenerTest extends BaseCardTest {

    @Test
    @DisplayName("Vivid reveals until it finds as many permanents as colors among your permanents")
    void revealsUntilRequiredNumberOfPermanents() {
        harness.addToBattlefield(player1, new AirElemental());
        Card shock = new Shock();
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(shock, bears, forest));

        castAuroraAwakener();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).extracting(Card::getId)
                .containsExactly(shock.getId(), bears.getId(), forest.getId());
        assertThat(choice.validCardIds()).containsExactly(bears.getId(), forest.getId());
    }

    @Test
    @DisplayName("Choosing any number puts the selected permanents onto the battlefield and bottoms the rest")
    void choosesAnyNumberAndBottomsTheRest() {
        Card shock = new Shock();
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(shock, bears, forest));

        castAuroraAwakener();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(bears.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(shock.getId(), forest.getId());
    }

    @Test
    @DisplayName("Choosing no permanents leaves every revealed card on the library bottom")
    void mayChooseNoPermanents() {
        Card shock = new Shock();
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(shock, bears));

        castAuroraAwakener();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(bears.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(shock.getId(), bears.getId());
    }

    @Test
    @DisplayName("If no permanent is found, the whole revealed library is put on the bottom")
    void noPermanentFound() {
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(shock));

        castAuroraAwakener();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(shock.getId());
    }

    private void castAuroraAwakener() {
        harness.setHand(player1, List.of(new AuroraAwakener()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
