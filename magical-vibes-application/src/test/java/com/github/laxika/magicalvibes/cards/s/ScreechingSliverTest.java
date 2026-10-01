package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BenalishCavalry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScreechingSliver.class, SidewinderSliver.class, BenalishCavalry.class})
class ScreechingSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Screeching Sliver grants itself the mill ability")
    void grantsAbilityToItself() {
        Permanent screechingSliver = addCreatureReady(player1, new ScreechingSliver());
        int deckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore - 1);
        assertThat(screechingSliver.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Screeching Sliver may target its controller")
    void millsItsController() {
        Permanent screechingSliver = addCreatureReady(player1, new ScreechingSliver());
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(screechingSliver.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Another Sliver gains the mill ability")
    void grantsAbilityToAnotherSliver() {
        addCreatureReady(player1, new ScreechingSliver());
        Permanent otherSliver = addCreatureReady(player1, new SidewinderSliver());
        int deckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore - 1);
        assertThat(otherSliver.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's Sliver also gains the mill ability")
    void grantsAbilityToOpposingSliver() {
        addCreatureReady(player1, new ScreechingSliver());
        Permanent opposingSliver = addCreatureReady(player2, new SidewinderSliver());
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(opposingSliver.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Non-Sliver creatures do not gain the mill ability")
    void doesNotGrantAbilityToNonSliver() {
        addCreatureReady(player1, new ScreechingSliver());
        addCreatureReady(player1, new BenalishCavalry());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
