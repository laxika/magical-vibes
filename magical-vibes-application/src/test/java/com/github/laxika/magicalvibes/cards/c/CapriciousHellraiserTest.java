package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.i.InfectiousInquiry;
import com.github.laxika.magicalvibes.cards.p.PhyrexianAtlas;
import com.github.laxika.magicalvibes.cards.v.Vivisection;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CapriciousHellraiser.class, InfectiousInquiry.class, CopperLonglegs.class, Forest.class, PhyrexianAtlas.class})
class CapriciousHellraiserTest extends BaseCardTest {

    @Test
    @DisplayName("costs three less to cast with nine cards in the graveyard")
    void costsLessWithNineCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest()));
        harness.castFromHand(player1, new CapriciousHellraiser(), "{R}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Capricious Hellraiser");
    }

    @Test
    @DisplayName("exiles three random cards, chooses an eligible card, and may cast its copy")
    void choosesEligibleCardAndCastsCopy() {
        InfectiousInquiry inquiry = new InfectiousInquiry();
        CopperLonglegs longlegs = new CopperLonglegs();
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(inquiry, longlegs, forest));
        harness.castFromHand(player1, new CapriciousHellraiser(), "{3}{R}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.ExiledSpellCopyChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.ExiledSpellCopyChoice.class);
        assertThat(choice.validCardIds()).containsExactly(inquiry.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(inquiry, longlegs, forest);

        harness.handleMultipleCardsChosen(player1, List.of(inquiry.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(inquiry, longlegs, forest);
    }

    @Test
    @DisplayName("declining the copy leaves the original selected card exiled")
    void decliningCopyLeavesOriginalExiled() {
        InfectiousInquiry inquiry = new InfectiousInquiry();
        harness.setGraveyard(player1, List.of(inquiry, new CopperLonglegs(), new Forest()));
        harness.castFromHand(player1, new CapriciousHellraiser(), "{3}{R}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(inquiry.getId()));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .filteredOn(card -> card.getId().equals(inquiry.getId()))
                .hasSize(1);
    }

    @Test
    @DisplayName("does not offer a copy when all three exiled cards are creatures or lands")
    void noEligibleCardMeansNoCopyChoice() {
        CopperLonglegs longlegs = new CopperLonglegs();
        Forest firstForest = new Forest();
        Forest secondForest = new Forest();
        harness.setGraveyard(player1, List.of(longlegs, firstForest, secondForest));
        harness.castFromHand(player1, new CapriciousHellraiser(), "{3}{R}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(longlegs, firstForest, secondForest);
    }

    @Test
    void eightCardsDoNotReduceTheCost() {
        harness.setGraveyard(player1, java.util.stream.IntStream.range(0, 8)
                .mapToObj(i -> new Forest()).toList());
        assertThatThrownBy(() -> harness.castFromHand(player1, new CapriciousHellraiser(), "{R}{R}{R}"))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsGraveyardDoesNotEnableReduction() {
        harness.setGraveyard(player2, java.util.stream.IntStream.range(0, 9)
                .mapToObj(i -> new Forest()).toList());
        assertThatThrownBy(() -> harness.castFromHand(player1, new CapriciousHellraiser(), "{R}{R}{R}"))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void emptyGraveyardFinishesWithoutAChoice() {
        harness.setGraveyard(player1, List.of());
        harness.castFromHand(player1, new CapriciousHellraiser(), "{3}{R}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Capricious Hellraiser");
    }

    @Test
    void oneCardGraveyardCanProduceAPermanentCopy() {
        PhyrexianAtlas atlas = new PhyrexianAtlas();
        harness.setGraveyard(player1, List.of(atlas));
        harness.castFromHand(player1, new CapriciousHellraiser(), "{3}{R}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(atlas.getId()));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Phyrexian Atlas");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Phyrexian Atlas"))
                .allSatisfy(p -> assertThat(p.getCard().isToken()).isTrue());
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(atlas);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void onlyThreeCardsAreExiledFromALargerGraveyard() {
        harness.setGraveyard(player1, java.util.stream.IntStream.range(0, 5)
                .mapToObj(i -> new Forest()).toList());
        harness.castFromHand(player1, new CapriciousHellraiser(), "{3}{R}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed({Vivisection.class})
    void payableMandatoryAdditionalCostIsOffered() {
        Vivisection vivisection = new Vivisection();
        harness.setGraveyard(player1, List.of(vivisection));
        harness.castFromHand(player1, new CapriciousHellraiser(), "{3}{R}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(vivisection.getId()));
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isNotNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(vivisection);
    }
}
