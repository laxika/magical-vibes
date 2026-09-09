package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AlabornTrooper;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.ManorGargoyle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RainOfDaggers.class, AlabornTrooper.class, Forest.class, ManorGargoyle.class})
class RainOfDaggersTest extends BaseCardTest {

    private static final int STARTING_LIFE = 20;

    @Test
    @DisplayName("Destroys all creatures the target opponent controls and controller loses 2 life each")
    void destroysOpponentCreaturesAndLosesLife() {
        harness.addToBattlefieldAndReturn(player2, new AlabornTrooper());
        harness.addToBattlefieldAndReturn(player2, new AlabornTrooper());
        harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addToBattlefieldAndReturn(player1, new AlabornTrooper());

        harness.setHand(player1, List.of(new RainOfDaggers()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Alaborn Trooper");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player1, "Alaborn Trooper");
        harness.assertLife(player1, STARTING_LIFE - 4);
    }

    @Test
    @DisplayName("Loses no life when the target opponent controls no creatures")
    void losesNoLifeWithNoCreatures() {
        harness.addToBattlefield(player1, new AlabornTrooper());

        harness.setHand(player1, List.of(new RainOfDaggers()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, STARTING_LIFE);
    }

    @Test
    @DisplayName("Indestructible creatures are not destroyed and do not count toward life loss")
    void indestructibleNotDestroyedNotCounted() {
        harness.addToBattlefieldAndReturn(player2, new AlabornTrooper());
        // Manor Gargoyle has defender, so its static ability makes it indestructible.
        harness.addToBattlefieldAndReturn(player2, new ManorGargoyle());

        harness.setHand(player1, List.of(new RainOfDaggers()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Alaborn Trooper");
        harness.assertOnBattlefield(player2, "Manor Gargoyle");
        harness.assertLife(player1, STARTING_LIFE - 2);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new RainOfDaggers()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }
}
