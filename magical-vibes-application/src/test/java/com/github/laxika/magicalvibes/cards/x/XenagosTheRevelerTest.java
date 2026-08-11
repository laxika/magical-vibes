package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class XenagosTheRevelerTest extends BaseCardTest {

    @Test
    @DisplayName("+1 adds one mana per creature in any combination of red and green")
    void plusOneAddsManaPerCreature() {
        Permanent xenagos = addReadyXenagos(3);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.RED.name());
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(xenagos.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("0 creates a hasty 2/2 red and green Satyr token")
    void zeroCreatesSatyrToken() {
        Permanent xenagos = addReadyXenagos(3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent satyr = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(satyr.getCard().getPower()).isEqualTo(2);
        assertThat(satyr.getCard().getToughness()).isEqualTo(2);
        assertThat(satyr.getCard().getSubtypes()).contains(CardSubtype.SATYR);
        assertThat(gqs.hasKeyword(gd, satyr, Keyword.HASTE)).isTrue();
        assertThat(xenagos.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("-6 exiles seven cards and puts chosen creatures and lands onto the battlefield")
    void minusSixPutsChosenCreaturesAndLandsOntoBattlefield() {
        Permanent xenagos = addReadyXenagos(6);
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        Card mountain = new Mountain();
        Card shock2 = new Shock();
        Card forest2 = new Forest();
        Card bears2 = new GrizzlyBears();
        setLibrary(forest, bears, shock, mountain, shock2, forest2, bears2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                forest.getId(), bears.getId(), mountain.getId(), forest2.getId(), bears2.getId());
        assertThat(choice.remainingToExile()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId(), bears.getId(), mountain.getId()));

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Mountain");
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Shock", "Shock", "Forest", "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(xenagos.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    @Test
    @DisplayName("-6 may put nothing onto the battlefield and exiles all seven cards")
    void minusSixMayPutNothing() {
        addReadyXenagos(6);
        Card shock = new Shock();
        setLibrary(shock, new Shock(), new Shock(), new Shock(), new Shock(), new Shock(), new Shock());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard().isToken());
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getName)
                .containsExactly("Shock", "Shock", "Shock", "Shock", "Shock", "Shock", "Shock");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private Permanent addReadyXenagos(int loyalty) {
        Permanent perm = new Permanent(new XenagosTheReveler());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private void setLibrary(Card... cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }
}
