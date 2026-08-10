package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TelJiladStylusTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a permanent you own on the bottom of your library")
    void putsOwnPermanentOnBottomOfLibrary() {
        harness.addToBattlefield(player1, new TelJiladStylus());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .doesNotContain(bears);
        assertThat(harness.getGameData().playerDecks.get(player1.getId()))
                .last()
                .extracting(Card::getName)
                .isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a permanent owned by an opponent")
    void cannotTargetOpponentPermanent() {
        harness.addToBattlefield(player1, new TelJiladStylus());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a permanent you own");
    }

    @Test
    @DisplayName("Can target a permanent you own while an opponent controls it")
    void putsStolenPermanentInItsOwnersLibrary() {
        harness.addToBattlefield(player1, new TelJiladStylus());
        Permanent stolenBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.getGameData().stolenCreatures.put(stolenBears.getId(), player1.getId());

        harness.activateAbility(player1, 0, null, stolenBears.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player2.getId()))
                .doesNotContain(stolenBears);
        assertThat(harness.getGameData().playerDecks.get(player1.getId()))
                .last()
                .extracting(Card::getName)
                .isEqualTo("Grizzly Bears");
        assertThat(harness.getGameData().playerDecks.get(player2.getId()))
                .doesNotContain(stolenBears.getCard());
    }
}
