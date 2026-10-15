package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AdultGoldDragon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PutridImp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EarthquakeDragon.class, AdultGoldDragon.class, Forest.class, PutridImp.class})
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

        harness.assertInHand(player1, "Earthquake Dragon");
        harness.assertNotInGraveyard(player1, "Earthquake Dragon");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    void sumsMultipleDragonsAndDoesNotReduceGreenRequirement() {
        harness.addToBattlefield(player1, new EarthquakeDragon());
        harness.addToBattlefield(player1, new EarthquakeDragon());
        harness.setHand(player1, List.of(new EarthquakeDragon()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Earthquake Dragon")).isEqualTo(3);
    }

    @Test
    void sumsManaValuesRatherThanCountingDragons() {
        harness.addToBattlefield(player1, new AdultGoldDragon());
        harness.addToBattlefield(player1, new AdultGoldDragon());
        harness.setHand(player1, List.of(new EarthquakeDragon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Earthquake Dragon");
    }

    @Test
    void opponentsDragonsDoNotReduceCost() {
        harness.addToBattlefield(player2, new EarthquakeDragon());
        harness.setHand(player1, List.of(new EarthquakeDragon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 13);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Earthquake Dragon");
    }

    @Test
    void cannotActivateBySacrificingAnOpponentsLandOrANonland() {
        harness.setGraveyard(player1, List.of(new EarthquakeDragon()));
        harness.addToBattlefield(player1, new EarthquakeDragon());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Earthquake Dragon");
        harness.assertOnBattlefield(player1, "Earthquake Dragon");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void choosesOnlyOneLandAndPaysSacrificeBeforeResolution() {
        harness.setGraveyard(player1, List.of(new EarthquakeDragon()));
        var chosenLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        var otherLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.handlePermanentChosen(player1, chosenLand.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(otherLand).doesNotContain(chosenLand);
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Earthquake Dragon");
        harness.assertNotInHand(player1, "Earthquake Dragon");

        harness.passBothPriorities();

        harness.assertInHand(player1, "Earthquake Dragon");
    }

    @Test
    void insufficientManaDoesNotSacrificeLand() {
        harness.setGraveyard(player1, List.of(new EarthquakeDragon()));
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Earthquake Dragon");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({EarthquakeDragon.class, Forest.class, PutridImp.class})
    void earlierActivationCannotReturnDragonThatLeftAndReenteredGraveyard() {
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(new EarthquakeDragon()));
        harness.addToBattlefield(player1, new PutridImp());
        var firstLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0);
        harness.handlePermanentChosen(player1, firstLand.getId());
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Earthquake Dragon");
        assertThat(gd.stack).hasSize(1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Earthquake Dragon");

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Earthquake Dragon");
        harness.assertNotInHand(player1, "Earthquake Dragon");
    }
}
