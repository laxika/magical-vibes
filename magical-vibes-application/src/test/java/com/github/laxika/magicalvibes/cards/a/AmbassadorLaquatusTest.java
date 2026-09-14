package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(AmbassadorLaquatus.class)
class AmbassadorLaquatusTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability mills three cards from the target player")
    void millsThreeCardsFromTargetPlayer() {
        addCreatureReady(player1, new AmbassadorLaquatus());
        harness.setLibrary(player2, List.of(
                new AmbassadorLaquatus(),
                new AmbassadorLaquatus(),
                new AmbassadorLaquatus(),
                new AmbassadorLaquatus()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Activated ability can target its controller")
    void canTargetItsController() {
        addCreatureReady(player1, new AmbassadorLaquatus());
        harness.setLibrary(player1, List.of(
                new AmbassadorLaquatus(),
                new AmbassadorLaquatus(),
                new AmbassadorLaquatus()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Cannot activate the ability without three generic mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new AmbassadorLaquatus());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        addCreatureReady(player1, new AmbassadorLaquatus());
        Permanent invalidTarget = addCreatureReady(player2, new AmbassadorLaquatus());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, invalidTarget.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a player");
    }
}
