package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;




@CardUsed({TirelessProvisioner.class, Forest.class})
class TirelessProvisionerTest extends BaseCardTest {

    @Test
    void landfallCanCreateFood() {
        addProvisionerAndLand();

        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "Create a Food token");
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    void landfallCanCreateTreasure() {
        addProvisionerAndLand();

        resolveAllTriggers();
        harness.handleListChoice(player1, "Create a Treasure token");
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    private void addProvisionerAndLand() {
        harness.addToBattlefield(player1, new TirelessProvisioner());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
    }
    private static final String TREASURE_MODE = "Create a Treasure token";

    private static final String FOOD_MODE = "Create a Food token";

    @Test
    void landfallCreatesFoodTokenWhenChosen() {
        harness.addToBattlefield(player1, new TirelessProvisioner());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        resolveAllTriggers();
        harness.handleListChoice(player1, FOOD_MODE);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
    }

    @Test
    void landfallCreatesTreasureTokenWhenChosen() {
        harness.addToBattlefield(player1, new TirelessProvisioner());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        resolveAllTriggers();
        harness.handleListChoice(player1, TREASURE_MODE);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    void opponentLandDoesNotTriggerLandfall() {
        harness.addToBattlefield(player1, new TirelessProvisioner());
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);

        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isZero();
    }

    @Test
    void tokenTypeIsChosenOnlyWhenLandfallResolves() {
        addProvisionerAndLand();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(countPermanents(player1, "Treasure")).isZero();

        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, FOOD_MODE);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
    }

    @Test
    void foodCanBeSacrificedImmediatelyForThreeLife() {
        addProvisionerAndLand();
        resolveAllTriggers();
        harness.handleListChoice(player1, FOOD_MODE);
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int foodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Food"));
        harness.activateAbility(player1, foodIndex, null, null);

        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        resolveAllTriggers();
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void treasureCanBeSacrificedImmediatelyForColoredMana() {
        addProvisionerAndLand();
        resolveAllTriggers();
        harness.handleListChoice(player1, TREASURE_MODE);
        resolveAllTriggers();

        int treasureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Treasure"));
        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void landEnteringWithoutBeingPlayedAlsoTriggers() {
        harness.addToBattlefield(player1, new TirelessProvisioner());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        resolveAllTriggers();
        harness.handleListChoice(player1, TREASURE_MODE);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    void foodRequiresTwoManaAndAnUntappedToken() {
        addProvisionerAndLand();
        resolveAllTriggers();
        harness.handleListChoice(player1, FOOD_MODE);
        resolveAllTriggers();
        int foodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Food"));

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, foodIndex, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        findPermanent(player1, "Food").tap();
        assertThatThrownBy(() -> harness.activateAbility(player1, foodIndex, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

}
