package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.PhantasmalTerrain;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeaSerpent.class, Island.class, Forest.class, PhantasmalTerrain.class})
class SeaSerpentTest extends BaseCardTest {

    // ===== State trigger: sacrifice when you control no Islands =====

    @Test
    @DisplayName("Sacrificed when controller controls no Islands")
    void sacrificedWhenNoIslands() {
        harness.setHand(player1, List.of(new SeaSerpent()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature → state trigger fires
        harness.passBothPriorities(); // resolve state trigger → sacrificed

        harness.assertNotOnBattlefield(player1, "Sea Serpent");
        harness.assertInGraveyard(player1, "Sea Serpent");
    }

    @Test
    @DisplayName("Survives while a controlled land has been changed into an Island")
    void survivesWithLandChangedIntoIsland() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent terrain = harness.addToBattlefieldAndReturn(player1, new PhantasmalTerrain());
        terrain.setAttachedTo(forest.getId());
        terrain.setChosenSubtype(CardSubtype.ISLAND);
        harness.addToBattlefield(player1, new SeaSerpent());

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).contains(CardSubtype.ISLAND);

        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Sea Serpent");
    }

    @Test
    @DisplayName("Survives while controller controls an Island")
    void survivesWithIsland() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new SeaSerpent()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Sea Serpent");
    }

    // ===== Attack restriction: defending player must control an Island =====

    @Test
    @DisplayName("Can attack when defending player controls an Island")
    void canAttackWhenDefenderControlsIsland() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Island()); // keep serpent alive
        harness.addToBattlefield(player2, new Island());

        addCreatureReady(player1, new SeaSerpent());

        declareAttackers(List.of(1));

        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Cannot attack when defending player controls no Island")
    void cannotAttackWhenDefenderHasNoIsland() {
        harness.addToBattlefield(player1, new Island()); // keep serpent alive

        addCreatureReady(player1, new SeaSerpent());

        assertThatThrownBy(() -> declareAttackers(List.of(1)))
                .isInstanceOf(IllegalStateException.class);
    }
}
