package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.PhantasmalTerrain;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeaSerpent.class, Island.class, Forest.class, PhantasmalTerrain.class, StoneRain.class})
class SeaSerpentTest extends BaseCardTest {
    @Test
    @DisplayName("Sacrificed when controller controls no Islands")
    void sacrificedWhenNoIslands() {
        harness.castFromHand(player1, new SeaSerpent(), "{5}{U}");
        harness.passBothPriorities(); // resolve creature → state trigger fires
        harness.passBothPriorities(); // resolve state trigger → sacrificed

        harness.assertNotOnBattlefield(player1, "Sea Serpent");
        harness.assertInGraveyard(player1, "Sea Serpent");
    }

    @Test
    @DisplayName("Survives while controller controls an Island")
    void survivesWithIsland() {
        harness.addToBattlefield(player1, new Island());
        harness.castFromHand(player1, new SeaSerpent(), "{5}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Sea Serpent");
    }

    @Test
    @DisplayName("Sacrifices when its last Island leaves the battlefield")
    void sacrificesWhenLastIslandLeavesBattlefield() {
        addCreatureReady(player1, new SeaSerpent());
        var island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, island.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sea Serpent");
        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Sea Serpent");
        harness.assertInGraveyard(player1, "Sea Serpent");
    }

    @Test
    @DisplayName("Survives while controller controls a land changed into an Island")
    void survivesWithTransformedIsland() {
        var forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new PhantasmalTerrain()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ISLAND");
        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.ISLAND);

        harness.castFromHand(player1, new SeaSerpent(), "{5}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Sea Serpent");
    }
    @Test
    @DisplayName("Can attack when defending player controls an Island")
    void canAttackWhenDefenderControlsIsland() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Island()); // keep serpent alive
        harness.addToBattlefield(player2, new Island());

        var serpent = addCreatureReady(player1, new SeaSerpent());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(serpent)));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Cannot attack when defending player controls no Island")
    void cannotAttackWhenDefenderHasNoIsland() {
        harness.addToBattlefield(player1, new Island()); // keep serpent alive

        var serpent = addCreatureReady(player1, new SeaSerpent());

        assertThatThrownBy(() -> declareAttackers(
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(serpent))))
                .isInstanceOf(IllegalStateException.class);
    }
}
