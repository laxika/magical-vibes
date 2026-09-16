package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.h.HickoryWoodlot;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RishadanPort;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CorneredMarket.class, CloudSprite.class, DarkRitual.class, HickoryWoodlot.class,
        Plains.class, RishadanPort.class})
class CorneredMarketTest extends BaseCardTest {

    @Test
    @DisplayName("Players can't cast spells sharing a name with a nontoken permanent")
    void preventsSpellsWithNontokenPermanentNames() {
        addReadyCorneredMarket(player1);
        harness.addToBattlefield(player1, new CloudSprite());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromHand(player2, new CloudSprite(), "{U}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Token names do not create a Cornered Market restriction")
    void ignoresTokenNames() {
        addReadyCorneredMarket(player1);
        harness.addToBattlefield(player1, tokenNamed("Cloud Sprite"));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new CloudSprite(), "{U}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The restriction also applies to the Cornered Market controller")
    void preventsControllerFromCastingMatchingSpell() {
        addReadyCorneredMarket(player1);
        harness.addToBattlefield(player1, new CloudSprite());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromHand(player1, new CloudSprite(), "{U}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cornered Market prevents matching nonbasic lands but not basic lands")
    void restrictsNonbasicLandsOnly() {
        addReadyCorneredMarket(player1);
        harness.addToBattlefield(player1, new HickoryWoodlot());
        harness.addToBattlefield(player1, new Plains());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new HickoryWoodlot()));

        assertThatThrownBy(() -> harness.playLand(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.setHand(player2, List.of(new Plains()));
        harness.playLand(player2, 0);

        harness.assertOnBattlefield(player2, "Plains");
    }

    @Test
    @DisplayName("A nonmatching nonbasic land can still be played")
    void allowsNonmatchingNonbasicLand() {
        addReadyCorneredMarket(player1);
        harness.addToBattlefield(player1, new HickoryWoodlot());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new RishadanPort()));

        harness.playLand(player2, 0);

        harness.assertOnBattlefield(player2, "Rishadan Port");
    }

    @Test
    @DisplayName("Names on the stack do not create a Cornered Market restriction")
    void ignoresNamesOnTheStack() {
        addReadyCorneredMarket(player1);
        harness.addToBattlefield(player1, new CloudSprite());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0);
        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("Matching spells cannot be cast from exile")
    void preventsMatchingSpellsFromExile() {
        addReadyCorneredMarket(player1);
        harness.addToBattlefield(player1, new CloudSprite());
        CloudSprite exiled = new CloudSprite();
        harness.setExile(player2, List.of(exiled));
        gd.exilePlayPermissions.put(exiled.getId(), player2.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castFromExile(player2, exiled.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Matching spells cannot be cast from the top of the library")
    void preventsMatchingSpellsFromLibraryTop() {
        addReadyCorneredMarket(player1);
        harness.addToBattlefield(player1, new CloudSprite());
        harness.setLibrary(player2, List.of(new CloudSprite()));
        gd.playersAllowedToPlayFromLibraryTopUntilEndOfTurn.add(player2.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Matching nonbasic lands cannot be played from exile")
    void preventsMatchingNonbasicLandsFromExile() {
        addReadyCorneredMarket(player1);
        harness.addToBattlefield(player1, new HickoryWoodlot());
        HickoryWoodlot exiled = new HickoryWoodlot();
        harness.setExile(player2, List.of(exiled));
        gd.exilePlayPermissions.put(exiled.getId(), player2.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromExile(player2, exiled.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private void addReadyCorneredMarket(Player player) {
        harness.addToBattlefield(player, new CorneredMarket());
    }

    private Card tokenNamed(String name) {
        Card token = new Card();
        token.setName(name);
        token.setType(CardType.CREATURE);
        token.setColor(CardColor.GREEN);
        token.setPower(1);
        token.setToughness(1);
        token.setToken(true);
        return token;
    }
}
