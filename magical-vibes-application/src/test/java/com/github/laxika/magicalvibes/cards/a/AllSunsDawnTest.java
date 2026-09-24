package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.EngineeredExplosives;
import com.github.laxika.magicalvibes.cards.f.FeedbackBolt;
import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.cards.n.NayaCharm;
import com.github.laxika.magicalvibes.cards.n.NightsWhisper;
import com.github.laxika.magicalvibes.cards.s.SerumVisions;
import com.github.laxika.magicalvibes.cards.t.TelJiladJustice;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AllSunsDawn.class, AuriokChampion.class, SerumVisions.class, NightsWhisper.class,
        FeedbackBolt.class, TelJiladJustice.class, NayaCharm.class, EngineeredExplosives.class,
        MycosynthLattice.class})
class AllSunsDawnTest extends BaseCardTest {

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    @Test
    void returnsUpToOneCardOfEachColorAndExilesItself() {
        List<Card> cards = List.of(
                new AuriokChampion(), new SerumVisions(), new NightsWhisper(), new FeedbackBolt(),
                new TelJiladJustice());
        harness.setGraveyard(player1, cards);
        harness.setHand(player1, List.of(new AllSunsDawn()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, cards.stream().map(Card::getId).toList());

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()).stream().map(Card::getId))
                .containsExactlyInAnyOrderElementsOf(cards.stream().map(Card::getId).toList());
        assertThat(gd.getPlayerExiledCards(player1.getId()).stream().map(card -> card.getName()))
                .contains("All Suns' Dawn");
    }

    @Test
    void multicoloredCardsCanFillDifferentColorGroups() {
        Card firstNayaCharm = new NayaCharm();
        Card secondNayaCharm = new NayaCharm();
        Card blueCard = new SerumVisions();
        harness.setGraveyard(player1, List.of(firstNayaCharm, secondNayaCharm, blueCard));
        harness.setHand(player1, List.of(new AllSunsDawn()));
        addMana();

        harness.castAndResolveSorcery(player1, 0,
                List.of(blueCard.getId(), firstNayaCharm.getId(), secondNayaCharm.getId()));

        assertThat(gd.playerHands.get(player1.getId()).stream().map(Card::getId))
                .containsExactlyInAnyOrder(blueCard.getId(), firstNayaCharm.getId(), secondNayaCharm.getId());
    }

    @Test
    void rejectsTwoCardsThatCanOnlyBeAssignedToTheSameColor() {
        Card firstWhiteCard = new AuriokChampion();
        Card secondWhiteCard = new AuriokChampion();
        harness.setGraveyard(player1, List.of(firstWhiteCard, secondWhiteCard));
        harness.setHand(player1, List.of(new AllSunsDawn()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(firstWhiteCard.getId(), secondWhiteCard.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one card for each color");
    }

    @Test
    void colorlessCardsAreNotLegalTargets() {
        Card colorlessCard = new EngineeredExplosives();
        harness.setGraveyard(player1, List.of(colorlessCard));
        harness.setHand(player1, List.of(new AllSunsDawn()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(colorlessCard.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mayResolveWithNoTargetsAndLeaveOtherGraveyardCardsAlone() {
        Card unselectedCard = new AuriokChampion();
        harness.setGraveyard(player1, List.of(unselectedCard));
        harness.setHand(player1, List.of(new AllSunsDawn()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(unselectedCard);
        assertThat(gd.getPlayerExiledCards(player1.getId()).stream().map(Card::getName))
                .contains("All Suns' Dawn");
    }

    @Test
    void returnsOnlySelectedColors() {
        Card whiteCard = new AuriokChampion();
        Card blackCard = new NightsWhisper();
        harness.setGraveyard(player1, List.of(whiteCard, blackCard));
        harness.setHand(player1, List.of(new AllSunsDawn()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(whiteCard.getId()));

        assertThat(gd.playerHands.get(player1.getId()).stream().map(Card::getId))
                .contains(whiteCard.getId())
                .doesNotContain(blackCard.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(blackCard);
        assertThat(gd.getPlayerExiledCards(player1.getId()).stream().map(Card::getName))
                .contains("All Suns' Dawn");
    }

    @Test
    void isCounteredIfAllTargetsBecomeColorlessBeforeResolution() {
        Card whiteCard = new AuriokChampion();
        Card allSunsDawn = new AllSunsDawn();
        harness.setGraveyard(player1, List.of(whiteCard));
        harness.setHand(player1, List.of(allSunsDawn));
        addMana();

        harness.castSorcery(player1, 0, List.of(whiteCard.getId()));
        harness.enterBattlefieldAndReturn(player1, new MycosynthLattice());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).stream().map(Card::getId))
                .doesNotContain(whiteCard.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()).stream().map(Card::getId))
                .containsExactlyInAnyOrder(whiteCard.getId(), allSunsDawn.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()).stream().map(Card::getName))
                .doesNotContain("All Suns' Dawn");
    }
}
