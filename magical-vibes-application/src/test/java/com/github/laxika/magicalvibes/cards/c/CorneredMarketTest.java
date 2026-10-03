package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.h.HickoryWoodlot;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RishadanPort;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CorneredMarket.class, CloudSprite.class, DarkRitual.class, Disenchant.class, HickoryWoodlot.class,
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
        harness.addToBattlefield(player1, cloudSpriteToken());

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
        harness.castFromHand(player1, new DarkRitual(), "{B}");
        harness.castFromHand(player1, new DarkRitual(), "{B}");

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

    @Test
    @DisplayName("Cornered Market's own name prevents another copy from being cast")
    void preventsAnotherCorneredMarket() {
        addReadyCorneredMarket(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromHand(player2, new CorneredMarket(), "{2}{W}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("A token with a nonbasic land's name does not prevent playing that land")
    void ignoresTokenNamesForNonbasicLands() {
        addReadyCorneredMarket(player1);
        HickoryWoodlot token = new HickoryWoodlot();
        token.setToken(true);
        harness.addToBattlefield(player1, token);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new HickoryWoodlot()));

        harness.playLand(player2, 0);

        harness.assertOnBattlefield(player2, "Hickory Woodlot");
    }

    @Test
    @DisplayName("Removing Cornered Market ends both restrictions")
    void restrictionsEndWhenMarketLeavesBattlefield() {
        Permanent market = harness.addToBattlefieldAndReturn(player1, new CorneredMarket());
        harness.addToBattlefield(player1, new CloudSprite());
        harness.addToBattlefield(player1, new HickoryWoodlot());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, market.getId());
        harness.assertNotOnBattlefield(player1, "Cornered Market");
        harness.setHand(player2, List.of(new HickoryWoodlot()));
        harness.playLand(player2, 0);
        harness.assertOnBattlefield(player2, "Hickory Woodlot");
        harness.castFromHand(player2, new CloudSprite(), "{U}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @CardUsed({Opalescence.class, Humble.class})
    @DisplayName("Losing its abilities ends Cornered Market's nonbasic-land restriction")
    void allowsMatchingNonbasicLandWhenMarketLosesAbilities() {
        Permanent market = harness.addToBattlefieldAndReturn(player1, new CorneredMarket());
        harness.addToBattlefield(player1, new Opalescence());
        harness.addToBattlefield(player1, new HickoryWoodlot());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Humble()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, market.getId());
        assertThat(gqs.hasLostAllAbilities(gd, market)).isTrue();
        harness.setHand(player2, List.of(new HickoryWoodlot()));
        harness.playLand(player2, 0);

        harness.assertOnBattlefield(player2, "Hickory Woodlot");
    }

    private void addReadyCorneredMarket(Player player) {
        harness.addToBattlefield(player, new CorneredMarket());
    }

    private Card cloudSpriteToken() {
        Card token = new CloudSprite();
        token.setToken(true);
        return token;
    }
}
