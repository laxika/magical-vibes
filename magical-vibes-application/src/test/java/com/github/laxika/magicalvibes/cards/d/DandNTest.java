package com.github.laxika.magicalvibes.cards.d;

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

@CardUsed({DandN.class, Island.class, Forest.class, PhantasmalTerrain.class})
class DandNTest extends BaseCardTest {
    @Test
    @DisplayName("Sacrificed when controller controls no Islands")
    void sacrificedWhenControllingNoIslands() {
        harness.setHand(player1, List.of(new DandN()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → state trigger fires

        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities(); // resolve state trigger → sacrificed
        harness.assertNotOnBattlefield(player1, "Dandân");
        harness.assertInGraveyard(player1, "Dandân");
    }

    @Test
    @DisplayName("Survives while controller controls an Island")
    void survivesWhileControllingIsland() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new DandN()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Dandân");
    }
    @Test
    @DisplayName("Can attack when defending player controls an Island")
    void canAttackWhenDefenderControlsIsland() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Island()); // keep Dandân from being sacrificed
        harness.addToBattlefield(player2, new Island());

        var dandan = addCreatureReady(player1, new DandN());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(dandan)));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Cannot attack when defending player controls no Island")
    void cannotAttackWhenDefenderControlsNoIsland() {
        harness.addToBattlefield(player1, new Island()); // keep Dandân from being sacrificed

        var dandan = addCreatureReady(player1, new DandN());

        assertThatThrownBy(() -> declareAttackers(
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(dandan))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A land changed into an Island satisfies the sacrifice condition")
    void transformedLandCountsAsIsland() {
        var forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new PhantasmalTerrain()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ISLAND");
        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.ISLAND);

        harness.setHand(player1, List.of(new DandN()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Dandân");
    }

    @Test
    @DisplayName("The sacrifice trigger does not recheck Islands when it resolves")
    void sacrificeTriggerDoesNotRecheckConditionOnResolution() {
        harness.setHand(player1, List.of(new DandN()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new Island());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dandân");
        harness.assertInGraveyard(player1, "Dandân");
    }

    @Test
    @DisplayName("An Island controlled by the opponent does not prevent the sacrifice")
    void opponentsIslandDoesNotPreventSacrifice() {
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new DandN()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Dandân");
        harness.assertInGraveyard(player1, "Dandân");
    }
}
