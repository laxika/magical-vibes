package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LegionVanguard.class, Forest.class, GrizzlyBears.class})
class LegionVanguardTest extends BaseCardTest {

    @Test
    @DisplayName("Pays one mana, sacrifices another creature, and explores into a land")
    void sacrificesAnotherCreatureAndExploresLand() {
        Permanent vanguard = addCreatureReady(player1, new LegionVanguard());
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(vanguard).doesNotContain(fodder);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(fodder.getCard());
        assertThat(gd.playerHands.get(player1.getId())).contains(land);
    }

    @Test
    @DisplayName("Exploring a nonland puts a counter on Legion Vanguard and offers the graveyard choice")
    void exploresNonland() {
        Permanent vanguard = addCreatureReady(player1, new LegionVanguard());
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(vanguard).doesNotContain(fodder);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(fodder.getCard(), topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot sacrifice Legion Vanguard itself")
    void requiresAnotherCreature() {
        addCreatureReady(player1, new LegionVanguard());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature to sacrifice");
    }
}
