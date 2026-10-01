package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HazelsBrewmaster.class, DrudgeSkeletons.class})
class HazelsBrewmasterTest extends BaseCardTest {

    @Test
    void entersExilesCreatureCreatesFoodAndFoodGainsItsActivatedAbility() {
        Card skeletons = new DrudgeSkeletons();
        harness.setGraveyard(player1, new ArrayList<>(List.of(skeletons)));
        harness.setHand(player1, List.of(new HazelsBrewmaster()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(skeletons.getId()));
        harness.passBothPriorities();

        Permanent hazel = findPermanent(player1, "Hazel's Brewmaster");
        Permanent food = findPermanent(player1, "Food");
        assertThat(gd.getCardsExiledByPermanent(hazel.getId())).containsExactly(skeletons);
        assertThat(gs.getEffectiveActivatedAbilities(gd, food)).hasSize(2);

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(food), 1, null, null);
        harness.passBothPriorities();

        assertThat(food.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void attackingExilesUpToOneCardAndCreatesFood() {
        Permanent hazel = new Permanent(new HazelsBrewmaster());
        hazel.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(hazel);
        Card skeletons = new DrudgeSkeletons();
        harness.setGraveyard(player2, List.of(skeletons));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(skeletons.getId()));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Food")).hasSize(1);
        assertThat(gd.getCardsExiledByPermanent(hazel.getId())).containsExactly(skeletons);
    }
}
