package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.z.ZephyrSpirit;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlazingArchon.class, ZephyrSpirit.class, Humble.class})
class BlazingArchonTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures cannot attack Blazing Archon's controller")
    void creaturesCannotAttackController() {
        harness.addToBattlefield(player2, new BlazingArchon());
        addCreatureReady(player1, new ZephyrSpirit());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("The restriction does not stop the controller's creatures from attacking an opponent")
    void creaturesCanAttackOpponent() {
        harness.addToBattlefield(player1, new BlazingArchon());
        addCreatureReady(player1, new ZephyrSpirit());

        declareAttackers(player1, List.of(1));
    }

    @Test
    @DisplayName("Flying creatures cannot attack Blazing Archon's controller either")
    void flyingCreaturesCannotAttackController() {
        harness.addToBattlefield(player2, new BlazingArchon());
        addCreatureReady(player1, new BlazingArchon());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("A tapped Blazing Archon still prevents attacks against its controller")
    void tappedArchonStillPreventsAttacks() {
        Permanent archon = harness.addToBattlefieldAndReturn(player2, new BlazingArchon());
        archon.setTapped(true);
        addCreatureReady(player1, new ZephyrSpirit());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Losing all abilities removes Blazing Archon's attack restriction")
    void losingAbilitiesAllowsAttacksAgainstController() {
        Permanent archon = harness.addToBattlefieldAndReturn(player2, new BlazingArchon());
        Permanent attacker = addCreatureReady(player1, new ZephyrSpirit());
        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, archon.getId());

        assertThat(gqs.hasLostAllAbilities(gd, archon)).isTrue();
        assertThat(als.canAttackDefender(gd, attacker, player2.getId())).isTrue();
        declareAttackers(player1, List.of(0));
    }
}
