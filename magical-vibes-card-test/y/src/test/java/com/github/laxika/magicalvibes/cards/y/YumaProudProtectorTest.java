package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.d.Desert;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FaithlessLooting;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SatyrWayfinder;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YumaProudProtector.class, Desert.class, Forest.class, GrizzlyBears.class,
        FaithlessLooting.class, SatyrWayfinder.class})
class YumaProudProtectorTest extends BaseCardTest {

    @Test
    @DisplayName("Costs one less for each land card in your graveyard")
    void costsOneLessForEachLandInGraveyard() {
        harness.setGraveyard(player1, List.of(new Forest(), new Desert()));
        harness.castFromHand(player1, new YumaProudProtector(), "{3}{R}{G}{W}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Entering may sacrifice a land and draw a card")
    void enteringMaySacrificeLandAndDraw() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.enterBattlefieldAndReturn(player1, new YumaProudProtector());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Attacking may sacrifice a land and draw a card")
    void attackingMaySacrificeLandAndDraw() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent yuma = harness.enterBattlefieldAndReturn(player1, new YumaProudProtector());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        yuma.setSummoningSick(false);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(yuma)));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Putting a Desert card into your graveyard creates a Plant Warrior with reach")
    void desertInGraveyardCreatesPlantWarrior() {
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new Desert());
        harness.enterBattlefieldAndReturn(player1, new YumaProudProtector());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, desert.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getCard().getSubtypes())
                .contains(CardSubtype.PLANT, CardSubtype.WARRIOR);
        assertThat(tokens.getFirst().getCard().getPower()).isEqualTo(4);
        assertThat(tokens.getFirst().getCard().getToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, tokens.getFirst(), Keyword.REACH)).isTrue();
    }

    @Test
    void decliningSacrificeDoesNotDraw() {
        harness.addToBattlefield(player1, new Forest());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.enterBattlefieldAndReturn(player1, new YumaProudProtector());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void acceptingWithoutLandDoesNotDraw() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.enterBattlefieldAndReturn(player1, new YumaProudProtector());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void manyLandsReduceOnlyGenericCost() {
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest()));

        harness.castFromHand(player1, new YumaProudProtector(), "{R}{G}{W}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void eachDesertPutIntoGraveyardFromLibraryCreatesAToken() {
        harness.addToBattlefield(player1, new YumaProudProtector());
        harness.setLibrary(player1, List.of(new Desert(), new Desert(), new Forest(), new GrizzlyBears()));
        harness.castFromHand(player1, new SatyrWayfinder(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(2);
    }

    @Test
    void nonDesertCardsDoNotTriggerGraveyardAbility() {
        harness.addToBattlefield(player1, new YumaProudProtector());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.castFromHand(player1, new SatyrWayfinder(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void discardingDesertCreatesToken() {
        harness.addToBattlefield(player1, new YumaProudProtector());
        harness.setLibrary(player1, List.of(new Desert(), new Forest()));
        harness.castFromHand(player1, new FaithlessLooting(), "{R}");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Desert");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(1);
    }
}
