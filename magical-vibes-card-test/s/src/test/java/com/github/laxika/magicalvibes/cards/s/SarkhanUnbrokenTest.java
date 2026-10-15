package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ColossodonYearling;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SarkhanUnbroken.class, ScionOfUgin.class, ColossodonYearling.class})
class SarkhanUnbrokenTest extends BaseCardTest {

    @Test
    @DisplayName("+1 draws a card and adds one mana of the chosen color")
    void plusOneDrawsAndAddsMana() {
        addReadySarkhan(player1, 3);
        ColossodonYearling drawn = new ColossodonYearling();
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("-2 creates a 4/4 red Dragon token with flying")
    void minusTwoCreatesDragonToken() {
        Permanent sarkhan = addReadySarkhan(player1, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent dragon = findPermanent(player1, "Dragon");
        assertThat(dragon.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(dragon.getCard().getPower()).isEqualTo(4);
        assertThat(dragon.getCard().getToughness()).isEqualTo(4);
        assertThat(dragon.getCard().getSubtypes()).contains(CardSubtype.DRAGON);
        assertThat(dragon.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(sarkhan.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("-8 puts any number of Dragon creature cards from the library onto the battlefield")
    void minusEightPutsDragonsOntoBattlefield() {
        addReadySarkhan(player1, 8);
        ScionOfUgin firstDragon = new ScionOfUgin();
        ScionOfUgin secondDragon = new ScionOfUgin();
        ColossodonYearling nonDragon = new ColossodonYearling();
        harness.setLibrary(player1, List.of(firstDragon, secondDragon, nonDragon));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).hasSize(2);
        assertThat(search.params().cards()).allMatch(card ->
                card.hasType(CardType.CREATURE) && card.getSubtypes().contains(CardSubtype.DRAGON));

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder("Scion of Ugin", "Scion of Ugin");
        harness.assertNotOnBattlefield(player1, "Sarkhan Unbroken");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonDragon);
    }

    @Test
    @DisplayName("+1 uses the stack and pays loyalty before drawing or adding mana")
    void plusOneWaitsForResolution() {
        Permanent sarkhan = addReadySarkhan(player1, 3);
        ColossodonYearling drawn = new ColossodonYearling();
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(sarkhan.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawn);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();

        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("-2 still creates the Dragon when paying loyalty removes Sarkhan")
    void minusTwoResolvesAfterSarkhanDies() {
        addReadySarkhan(player1, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sarkhan Unbroken");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof SarkhanUnbroken);
        harness.assertOnBattlefield(player1, "Dragon");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("-8 can find zero Dragons even when matching cards exist")
    void minusEightCanChooseZeroDragons() {
        addReadySarkhan(player1, 9);
        ScionOfUgin dragon = new ScionOfUgin();
        harness.setLibrary(player1, List.of(dragon));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Scion of Ugin");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(dragon);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("-8 can stop after selecting only some Dragons")
    void minusEightCanChooseSubsetOfDragons() {
        addReadySarkhan(player1, 9);
        ScionOfUgin chosen = new ScionOfUgin();
        ScionOfUgin unchosen = new ScionOfUgin();
        ColossodonYearling nonDragon = new ColossodonYearling();
        harness.setLibrary(player1, List.of(chosen, unchosen, nonDragon));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).contains(chosen).doesNotContain(unchosen, nonDragon);
        assertThat(findPermanent(player1, "Scion of Ugin").isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(unchosen, nonDragon);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("-8 finishes when the library contains no Dragons")
    void minusEightWithNoMatchingCards() {
        addReadySarkhan(player1, 9);
        ColossodonYearling nonDragon = new ColossodonYearling();
        harness.setLibrary(player1, List.of(nonDragon));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonDragon);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("-8 finishes with an empty library")
    void minusEightWithEmptyLibrary() {
        addReadySarkhan(player1, 9);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("-8 puts the selected Dragons onto the battlefield together after selection ends")
    void minusEightWaitsUntilAllDragonsAreSelected() {
        addReadySarkhan(player1, 9);
        ScionOfUgin firstDragon = new ScionOfUgin();
        ScionOfUgin secondDragon = new ScionOfUgin();
        harness.setLibrary(player1, List.of(firstDragon, secondDragon));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).doesNotContain(firstDragon, secondDragon);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).contains(firstDragon, secondDragon);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
    private Permanent addReadySarkhan(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new SarkhanUnbroken());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return permanent;
    }
}
