package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WormsOfTheEarth.class, Forest.class})
class WormsOfTheEarthTest extends BaseCardTest {

    @Test
    @DisplayName("Worms of the Earth prevents land plays")
    void preventsLandPlays() {
        harness.addToBattlefield(player1, new WormsOfTheEarth());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Forest()));
        harness.clearPriorityPassed();
        harness.ensurePriority(player1);

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing two lands destroys Worms of the Earth")
    void sacrificingTwoLandsDestroysIt() {
        harness.addToBattlefield(player1, new WormsOfTheEarth());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Worms of the Earth");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Choosing the sacrifice option lets the player choose which two lands to sacrifice")
    void choosesTwoLandsWhenMoreAreAvailable() {
        harness.addToBattlefield(player1, new WormsOfTheEarth());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        List<java.util.UUID> forestIds = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Forest"))
                .map(permanent -> permanent.getId())
                .toList();
        harness.handleMultiplePermanentsChosen(player1, forestIds.subList(0, 2));

        harness.assertNotOnBattlefield(player1, "Worms of the Earth");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Forest"))
                .hasSize(1);
    }

    @Test
    @DisplayName("Accepting the damage option deals 5 damage and destroys Worms of the Earth")
    void acceptingDamageDestroysIt() {
        harness.addToBattlefield(player1, new WormsOfTheEarth());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 15);
        harness.assertNotOnBattlefield(player1, "Worms of the Earth");
    }

    @Test
    @DisplayName("Declining both upkeep choices leaves Worms of the Earth in play")
    void decliningBothChoicesKeepsIt() {
        harness.addToBattlefield(player1, new WormsOfTheEarth());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Worms of the Earth");
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }
}
