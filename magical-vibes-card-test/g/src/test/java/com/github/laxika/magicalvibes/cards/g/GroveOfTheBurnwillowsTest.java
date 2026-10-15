package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GroveOfTheBurnwillows.class})
class GroveOfTheBurnwillowsTest extends BaseCardTest {

    @Test
    void tappingForColorlessManaDoesNotGiveLife() {
        Permanent grove = harness.addToBattlefieldAndReturn(player1, new GroveOfTheBurnwillows());
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(grove.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    void tappingForRedManaGivesOpponentLife() {
        Permanent grove = harness.addToBattlefieldAndReturn(player1, new GroveOfTheBurnwillows());
        int controllerLifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("RED", "GREEN");
        harness.handleListChoice(player1, "RED");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore + 1);
        assertThat(grove.isTapped()).isTrue();
    }

    @Test
    void tappingForGreenManaGivesOpponentLife() {
        Permanent grove = harness.addToBattlefieldAndReturn(player1, new GroveOfTheBurnwillows());
        int controllerLifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("RED", "GREEN");
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore + 1);
        assertThat(grove.isTapped()).isTrue();
    }
    @Test
    void coloredManaGivesLifeToTheActivatingPlayersOpponent() {
        Permanent grove = harness.addToBattlefieldAndReturn(player2, new GroveOfTheBurnwillows());
        int controllerLifeBefore = gd.getLife(player2.getId());
        int opponentLifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player2, 0, 1, null, null);
        harness.handleListChoice(player2, "GREEN");

        assertThat(grove.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(controllerLifeBefore);
        assertThat(gd.getLife(player1.getId())).isEqualTo(opponentLifeBefore + 1);
    }

    @Test
    void tappedGroveCannotActivateEitherManaAbility() {
        Permanent grove = harness.addToBattlefieldAndReturn(player1, new GroveOfTheBurnwillows());
        grove.tap();
        int opponentLifeBefore = gd.getLife(player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
