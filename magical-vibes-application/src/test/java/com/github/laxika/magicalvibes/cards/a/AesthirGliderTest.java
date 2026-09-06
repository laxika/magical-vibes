package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.SwornDefender;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AesthirGlider.class, SwornDefender.class})
class AesthirGliderTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Aesthir Glider puts it on the battlefield")
    void castingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new AesthirGlider()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).get(0).getCard())
                .isInstanceOf(AesthirGlider.class);
    }

    @Test
    @DisplayName("Aesthir Glider cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        addCreatureReady(player2, new AesthirGlider());

        addCreatureReady(player1, new SwornDefender());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Flying prevents a nonflying creature from blocking Aesthir Glider")
    void flyingPreventsNonflyingCreatureFromBlocking() {
        addCreatureReady(player1, new AesthirGlider());
        addCreatureReady(player2, new SwornDefender());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }
}
