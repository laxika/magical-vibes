package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AvenArcher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InfectedVermin.class, AvenArcher.class, InnocentBlood.class})
class InfectedVerminTest extends BaseCardTest {

    @Test
    @DisplayName("Base ability deals 1 damage to each creature and each player")
    void baseAbilityDealsOneDamageToEachCreatureAndPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new InfectedVermin());
        harness.addToBattlefield(player2, new InfectedVermin());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        harness.assertNotOnBattlefield(player2, "Infected Vermin");
        harness.assertInGraveyard(player1, "Infected Vermin");
    }

    @Test
    @DisplayName("Base ability deals only 1 damage to creatures")
    void baseAbilityLeavesTwoToughnessCreatureAlive() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new InfectedVermin());
        harness.addToBattlefield(player2, new AvenArcher());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        harness.assertOnBattlefield(player2, "Aven Archer");
    }

    @Test
    @DisplayName("Threshold ability deals 3 damage to each creature and each player")
    void thresholdAbilityDealsThreeDamageToEachCreatureAndPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new InfectedVermin());
        harness.addToBattlefield(player2, new AvenArcher());
        harness.setGraveyard(player1, List.of(
                new InfectedVermin(), new InfectedVermin(), new InfectedVermin(), new InfectedVermin(),
                new InfectedVermin(), new InfectedVermin(), new InnocentBlood()
        ));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
        harness.assertNotOnBattlefield(player2, "Aven Archer");
        harness.assertInGraveyard(player1, "Infected Vermin");
    }

    @Test
    @DisplayName("Threshold ability cannot be activated below seven graveyard cards")
    void thresholdAbilityCannotBeActivatedBelowSevenCards() {
        harness.addToBattlefield(player1, new InfectedVermin());
        harness.setGraveyard(player1, List.of(
                new InfectedVermin(), new InfectedVermin(), new InfectedVermin(),
                new InfectedVermin(), new InfectedVermin(), new InfectedVermin()
        ));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("seven or more cards");
    }

    @Test
    @DisplayName("Threshold checks the activating player's graveyard")
    void thresholdUsesActivatingPlayersGraveyard() {
        harness.addToBattlefield(player1, new InfectedVermin());
        harness.setGraveyard(player2, List.of(
                new InfectedVermin(), new InfectedVermin(), new InfectedVermin(), new InfectedVermin(),
                new InfectedVermin(), new InfectedVermin(), new InfectedVermin()
        ));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("seven or more cards");
    }
}
