package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Asmoranomardicadaistinaculdacar.class, GrizzlyBears.class})
class AsmoranomardicadaistinaculdacarTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast for {B/R} after discarding a card and searches for The Underworld Cookbook")
    void castsAfterDiscardingAndSearchesForCookbook() {
        harness.setHand(player1, List.of(new Asmoranomardicadaistinaculdacar()));
        harness.setLibrary(player1, List.of(cookbook(), new GrizzlyBears()));
        gd.cardsDiscardedThisTurn.put(player1.getId(), 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertOnBattlefield(player1, "Asmoranomardicadaistinaculdacar");
        harness.assertInHand(player1, "The Underworld Cookbook");
    }

    @Test
    @DisplayName("The alternate cost requires a discard this turn")
    void alternateCostRequiresDiscard() {
        harness.setHand(player1, List.of(new Asmoranomardicadaistinaculdacar()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrificing two Foods makes a target creature deal 6 damage to itself")
    void sacrificesTwoFoodsToDestroyTargetCreature() {
        Permanent asmoranomardicadaistinaculdacar =
                harness.addToBattlefieldAndReturn(player1, new Asmoranomardicadaistinaculdacar());
        harness.addToBattlefield(player1, foodToken());
        harness.addToBattlefield(player1, foodToken());
        harness.addToBattlefield(player1, foodToken());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(asmoranomardicadaistinaculdacar),
                null,
                target.getId());
        List<Permanent> foods = findPermanents(player1, "Food");
        harness.handlePermanentChosen(player1, foods.get(0).getId());
        harness.handlePermanentChosen(player1, foods.get(1).getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isOne();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    private Card cookbook() {
        Card cookbook = new Card();
        cookbook.setName("The Underworld Cookbook");
        cookbook.setType(CardType.ARTIFACT);
        cookbook.setManaCost("{1}");
        return cookbook;
    }

    private Card foodToken() {
        Card food = new Card();
        food.setName("Food");
        food.setType(CardType.ARTIFACT);
        food.setToken(true);
        food.setSubtypes(List.of(CardSubtype.FOOD));
        return food;
    }
}
