package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZanikevLocust;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BonecacheOverseer.class, GrizzlyBears.class, ZanikevLocust.class})
class BonecacheOverseerTest extends BaseCardTest {

    @Test
    @DisplayName("Requires three cards to have left the graveyard")
    void requiresThreeCardsToLeaveGraveyard() {
        addCreatureReady(player1, new BonecacheOverseer());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player1, List.of(new ZanikevLocust(), new ZanikevLocust(), new ZanikevLocust()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        leaveGraveyardCard(target);
        leaveGraveyardCard(target);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Activate only if");

        leaveGraveyardCard(target);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A sacrificed Food enables the draw ability")
    void sacrificedFoodEnablesDraw() {
        addCreatureReady(player1, new BonecacheOverseer());
        addFoodToken(player1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertNotOnBattlefield(player1, "Food");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void leaveGraveyardCard(Permanent target) {
        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addFoodToken(Player player) {
        Card food = new Card();
        food.setName("Food");
        food.setType(CardType.ARTIFACT);
        food.setManaCost("");
        food.setToken(true);
        food.setColor(null);
        food.setSubtypes(List.of(CardSubtype.FOOD));
        food.addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
                "{2}, {T}, Sacrifice this token: You gain 3 life."
        ));

        Permanent foodPermanent = new Permanent(food);
        foodPermanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(foodPermanent);
    }
}
