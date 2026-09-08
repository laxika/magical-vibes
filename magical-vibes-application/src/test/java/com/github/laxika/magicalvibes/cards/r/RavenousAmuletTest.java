package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RavenousAmuletTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature draws a card and adds a soul counter")
    void sacrificingCreatureDrawsAndAddsSoulCounter() {
        Permanent amulet = harness.addToBattlefieldAndReturn(player1, new RavenousAmulet());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        setLibrary(new LlanowarElves());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof LlanowarElves);
        assertThat(amulet.getCounterCount(CounterType.SOUL)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature.getCard());
    }

    @Test
    @DisplayName("Sacrificing the amulet makes each opponent lose life equal to its soul counters")
    void sacrificingAmuletMakesEachOpponentLoseSoulCounters() {
        Permanent amulet = harness.addToBattlefieldAndReturn(player1, new RavenousAmulet());
        amulet.setCounterCount(CounterType.SOUL, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(amulet);

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(amulet.getCard());
    }

    @Test
    @DisplayName("The draw ability requires a creature to sacrifice")
    void drawAbilityRequiresCreatureToSacrifice() {
        harness.addToBattlefield(player1, new RavenousAmulet());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void setLibrary(Card card) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.add(card);
    }
}
