package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TelJiladStylus.class, Ornithopter.class, Forest.class})
class TelJiladStylusTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a permanent you own on the bottom of your library")
    void putsOwnPermanentOnBottomOfLibrary() {
        harness.addToBattlefield(player1, new TelJiladStylus());
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        harness.activateAbility(player1, 0, null, ornithopter.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .doesNotContain(ornithopter);
        assertThat(harness.getGameData().playerDecks.get(player1.getId()))
                .last()
                .extracting(Card::getName)
                .isEqualTo("Ornithopter");
    }

    @Test
    @DisplayName("Can target itself as a permanent")
    void putsItselfOnBottomOfLibrary() {
        Permanent stylus = harness.addToBattlefieldAndReturn(player1, new TelJiladStylus());

        harness.activateAbility(player1, 0, null, stylus.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .doesNotContain(stylus);
        assertThat(harness.getGameData().playerDecks.get(player1.getId()))
                .last()
                .extracting(Card::getName)
                .isEqualTo("Tel-Jilad Stylus");
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
        Permanent stolenOrnithopter = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.getGameData().stolenCreatures.put(stolenOrnithopter.getId(), player1.getId());

        harness.activateAbility(player1, 0, null, stolenOrnithopter.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player2.getId()))
                .doesNotContain(stolenOrnithopter);
        assertThat(harness.getGameData().playerDecks.get(player1.getId()))
                .last()
                .extracting(Card::getName)
                .isEqualTo("Ornithopter");
        assertThat(harness.getGameData().playerDecks.get(player2.getId()))
                .doesNotContain(stolenOrnithopter.getCard());
    }
}
