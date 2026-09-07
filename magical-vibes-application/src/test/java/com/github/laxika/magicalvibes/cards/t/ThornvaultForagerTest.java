package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SquirrelMob;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThornvaultForager.class, GrizzlyBears.class, SquirrelMob.class})
class ThornvaultForagerTest extends BaseCardTest {

    @Test
    void firstAbilityAddsGreenMana() {
        Permanent forager = addReadyForager();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(forager.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void secondAbilityForagesByExilingThreeCardsAndAddsTwoChosenMana() {
        addReadyForager();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).hasSize(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void secondAbilityCanForageBySacrificingFood() {
        addReadyForager();
        Permanent food = addFoodToken();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, food.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(food);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    void thirdAbilitySearchesForASquirrelCard() {
        addReadyForager();
        harness.setLibrary(player1, List.of(new SquirrelMob(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Squirrel Mob");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears");
    }

    private Permanent addReadyForager() {
        Permanent forager = harness.addToBattlefieldAndReturn(player1, new ThornvaultForager());
        forager.setSummoningSick(false);
        return forager;
    }

    private Permanent addFoodToken() {
        Card food = new Card();
        food.setName("Food");
        food.setType(CardType.ARTIFACT);
        food.setManaCost("");
        food.setToken(true);
        food.setSubtypes(List.of(CardSubtype.FOOD));

        Permanent permanent = new Permanent(food);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }
}
