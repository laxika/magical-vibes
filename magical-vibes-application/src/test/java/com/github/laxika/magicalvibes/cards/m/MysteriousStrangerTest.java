package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MysteriousStranger.class, CounselOfTheSoratami.class, GrizzlyBears.class})
class MysteriousStrangerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles one instant or sorcery from each graveyard and casts one random copy for free")
    void exilesFromEachGraveyardAndCastsOneCopy() {
        CounselOfTheSoratami ownCounsel = new CounselOfTheSoratami();
        CounselOfTheSoratami opponentCounsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(ownCounsel));
        harness.setGraveyard(player2, List.of(opponentCounsel));
        harness.setHand(player1, List.of(new MysteriousStranger()));
        addStrangerMana();

        harness.castCreature(player1, 0, List.of(ownCounsel.getId(), opponentCounsel.getId()));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(ownCounsel.getId(), opponentCounsel.getId()));
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(ownCounsel.getId())).isNotNull();
        assertThat(gd.findExiledCard(opponentCounsel.getId())).isNotNull();
        assertThat(gd.stack).filteredOn(StackEntry::isCopy)
                .singleElement()
                .extracting(entry -> entry.getCard().getName())
                .isEqualTo("Counsel of the Soratami");
    }

    @Test
    @DisplayName("Does not cast a copy when only one eligible graveyard is targeted")
    void doesNotCastCopyWithOnlyOneExiledCard() {
        CounselOfTheSoratami ownCounsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(ownCounsel));
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new MysteriousStranger()));
        addStrangerMana();

        harness.castCreature(player1, 0, List.of(ownCounsel.getId()));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(ownCounsel.getId()));
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(ownCounsel.getId())).isNotNull();
        assertThat(gd.stack).noneMatch(StackEntry::isCopy);
    }

    @Test
    @DisplayName("Cannot target a creature card in a graveyard")
    void rejectsCreatureCardTarget() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new MysteriousStranger()));
        addStrangerMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addStrangerMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
