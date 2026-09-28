package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.d.Desert;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YumaProudProtector.class, Desert.class, Forest.class, GrizzlyBears.class})
class YumaProudProtectorTest extends BaseCardTest {

    @Test
    @DisplayName("Costs one less for each land card in your graveyard")
    void costsOneLessForEachLandInGraveyard() {
        harness.setGraveyard(player1, List.of(new Forest(), new Desert()));
        harness.setHand(player1, List.of(new YumaProudProtector()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);

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
}
