package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AdultGoldDragon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EarthquakeDragon.class, AdultGoldDragon.class, Forest.class})
class EarthquakeDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Costs less to cast by the total mana value of Dragons you control")
    void dragonManaValuesReduceCastCost() {
        harness.addToBattlefield(player1, new AdultGoldDragon());
        harness.setHand(player1, List.of(new EarthquakeDragon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Earthquake Dragon");
    }

    @Test
    @DisplayName("Its graveyard ability sacrifices a land and returns it to hand")
    void graveyardAbilitySacrificesLandAndReturnsToHand() {
        EarthquakeDragon dragon = new EarthquakeDragon();
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(dragon));
        harness.addToBattlefield(player1, forest);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(dragon);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(dragon);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(forest.getId()));
    }
}
