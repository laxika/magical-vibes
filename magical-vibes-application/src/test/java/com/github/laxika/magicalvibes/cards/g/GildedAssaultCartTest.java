package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GildedAssaultCart.class, GrizzlyBears.class})
class GildedAssaultCartTest extends BaseCardTest {

    @Test
    @DisplayName("Crew 2 animates Gilded Assault Cart and taps the crew")
    void crewAnimatesVehicleAndTapsCrew() {
        Permanent cart = addCreatureReady(player1, new GildedAssaultCart());
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, cart)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing two Treasures returns Gilded Assault Cart from the graveyard to hand")
    void sacrificingTwoTreasuresReturnsFromGraveyardToHand() {
        harness.setGraveyard(player1, List.of(new GildedAssaultCart()));
        addTreasure(player1);
        addTreasure(player1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Gilded Assault Cart");
        harness.assertNotInGraveyard(player1, "Gilded Assault Cart");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("The graveyard ability cannot sacrifice non-Treasures")
    void graveyardAbilityRequiresTwoTreasures() {
        harness.setGraveyard(player1, List.of(new GildedAssaultCart()));
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Gilded Assault Cart");
    }

    private Permanent addTreasure(Player player) {
        Card treasure = new Card();
        treasure.setName("Treasure");
        treasure.setType(CardType.ARTIFACT);
        treasure.setSubtypes(List.of(CardSubtype.TREASURE));
        treasure.setToken(true);
        return addCreatureReady(player, treasure);
    }
}
