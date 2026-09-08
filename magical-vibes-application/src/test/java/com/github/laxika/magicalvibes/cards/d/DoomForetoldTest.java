package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoomForetold.class, Forest.class, GrizzlyBears.class})
class DoomForetoldTest extends BaseCardTest {

    @Test
    @DisplayName("The active player sacrifices a nonland, nontoken permanent when able")
    void sacrificesMatchingPermanent() {
        Permanent doom = harness.addToBattlefieldAndReturn(player1, new DoomForetold());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(doom);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(countNamedPermanents(player1, "Knight")).isZero();
    }

    @Test
    @DisplayName("The active player chooses which matching permanent to sacrifice")
    void choosesMatchingPermanent() {
        harness.addToBattlefield(player1, new DoomForetold());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultiplePermanentsChosen(player2, List.of(second.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first).doesNotContain(second);
    }

    @Test
    @DisplayName("If the active player cannot sacrifice, the fallback effects resolve in order")
    void resolvesFallbackWhenNoMatchingPermanentExists() {
        Permanent doom = harness.addToBattlefieldAndReturn(player1, new DoomForetold());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        int initialControllerHandSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 22);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(initialControllerHandSize + 1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(doom);
        assertThat(countNamedPermanents(player1, "Knight")).isOne();
        Permanent knight = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> "Knight".equals(permanent.getCard().getName()))
                .findFirst()
                .orElseThrow();
        assertThat(knight.getCard().hasKeyword(Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("During its controller's upkeep, Doom Foretold can be sacrificed to its own ability")
    void sacrificesItselfWhenItIsTheOnlyMatchingPermanent() {
        harness.addToBattlefield(player1, new DoomForetold());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Doom Foretold");
        harness.assertLife(player1, 20);
        assertThat(countNamedPermanents(player1, "Knight")).isZero();
    }

    private long countNamedPermanents(com.github.laxika.magicalvibes.model.Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .map(Permanent::getCard)
                .map(Card::getName)
                .filter(name::equals)
                .count();
    }
}
