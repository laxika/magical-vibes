package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
        harness.castFromHand(player1, new MysteriousStranger(), "{2}{R}{R}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(ownCounsel.getId(), opponentCounsel.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

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
        harness.castFromHand(player1, new MysteriousStranger(), "{2}{R}{R}");
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
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(bears, counsel));
        harness.castFromHand(player1, new MysteriousStranger(), "{2}{R}{R}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The controller may decline to cast the random copy while both originals remain exiled")
    void mayDeclineRandomCopy() {
        CounselOfTheSoratami ownCounsel = new CounselOfTheSoratami();
        CounselOfTheSoratami opponentCounsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(ownCounsel));
        harness.setGraveyard(player2, List.of(opponentCounsel));
        harness.castFromHand(player1, new MysteriousStranger(), "{2}{R}{R}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(ownCounsel.getId(), opponentCounsel.getId()));
        harness.passBothPriorities();

        assertThat(gd.stack).noneMatch(StackEntry::isCopy);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.findExiledCard(ownCounsel.getId())).isNotNull();
        assertThat(gd.findExiledCard(opponentCounsel.getId())).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("No copy is cast if one of two targets leaves its graveyard before resolution")
    void countsOnlyCardsActuallyExiled() {
        CounselOfTheSoratami ownCounsel = new CounselOfTheSoratami();
        CounselOfTheSoratami opponentCounsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(ownCounsel));
        harness.setGraveyard(player2, List.of(opponentCounsel));
        harness.castFromHand(player1, new MysteriousStranger(), "{2}{R}{R}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(ownCounsel.getId(), opponentCounsel.getId()));
        harness.setGraveyard(player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(ownCounsel.getId())).isNotNull();
        assertThat(gd.findExiledCard(opponentCounsel.getId())).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Cannot choose two cards from the same graveyard")
    void rejectsTwoCardsFromSameGraveyard() {
        CounselOfTheSoratami first = new CounselOfTheSoratami();
        CounselOfTheSoratami second = new CounselOfTheSoratami();
        CounselOfTheSoratami opponentCounsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setGraveyard(player2, List.of(opponentCounsel));
        harness.castFromHand(player1, new MysteriousStranger(), "{2}{R}{R}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Must choose a card from every eligible graveyard")
    void cannotOmitEligibleGraveyard() {
        CounselOfTheSoratami ownCounsel = new CounselOfTheSoratami();
        CounselOfTheSoratami opponentCounsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(ownCounsel));
        harness.setGraveyard(player2, List.of(opponentCounsel));
        harness.castFromHand(player1, new MysteriousStranger(), "{2}{R}{R}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(ownCounsel.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enters without a target choice when neither graveyard has an instant or sorcery")
    void noEligibleGraveyards() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of());
        harness.castFromHand(player1, new MysteriousStranger(), "{2}{R}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mysterious Stranger");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).noneMatch(StackEntry::isCopy);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

}
