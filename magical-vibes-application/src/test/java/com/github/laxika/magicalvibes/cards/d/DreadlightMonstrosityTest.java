package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DreadlightMonstrosity.class, GrizzlyBears.class})
class DreadlightMonstrosityTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot activate without owning a card in exile")
    void cannotActivateWithoutOwningCardInExile() {
        addCreatureReady(player1, new DreadlightMonstrosity());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("own a card in exile");
    }

    @Test
    @DisplayName("Own card in exile enables the unblockable ability")
    void ownCardInExileEnablesAbility() {
        Permanent monstrosity = addCreatureReady(player1, new DreadlightMonstrosity());
        addCreatureReady(player2, new GrizzlyBears());
        harness.getGameData().addToExile(player1.getId(), new GrizzlyBears());
        addManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(monstrosity.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("An opponent's exiled card does not satisfy the activation condition")
    void opponentsExiledCardDoesNotEnableAbility() {
        addCreatureReady(player1, new DreadlightMonstrosity());
        harness.getGameData().addToExile(player2.getId(), new GrizzlyBears());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("own a card in exile");
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
