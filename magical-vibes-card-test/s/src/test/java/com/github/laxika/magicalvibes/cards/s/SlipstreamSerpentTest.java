package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlipstreamSerpent.class, Island.class})
class SlipstreamSerpentTest extends BaseCardTest {

    @Test
    void isSacrificedWhenControllerControlsNoIslands() {
        harness.castFromHand(player1, new SlipstreamSerpent(), "{7}{U}");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Slipstream Serpent");
        harness.assertInGraveyard(player1, "Slipstream Serpent");
    }

    @Test
    void survivesWhileControllerControlsAnIsland() {
        harness.addToBattlefield(player1, new Island());
        harness.castFromHand(player1, new SlipstreamSerpent(), "{7}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Slipstream Serpent");
    }

    @Test
    void isSacrificedAfterControllerLosesLastIsland() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.castFromHand(player1, new SlipstreamSerpent(), "{7}{U}");
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(island);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Slipstream Serpent");
        harness.assertInGraveyard(player1, "Slipstream Serpent");
    }

    @Test
    void isSacrificedWhenOnlyOpponentControlsAnIsland() {
        harness.addToBattlefield(player2, new Island());
        harness.castFromHand(player1, new SlipstreamSerpent(), "{7}{U}");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Slipstream Serpent");
        harness.assertInGraveyard(player1, "Slipstream Serpent");
    }

    @Test
    void faceDownIslandDoesNotSatisfyIslandCondition() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        island.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        harness.castFromHand(player1, new SlipstreamSerpent(), "{7}{U}");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Slipstream Serpent");
        harness.assertInGraveyard(player1, "Slipstream Serpent");
    }

    @Test
    void canAttackWhenDefendingPlayerControlsAnIsland() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());

        Permanent serpent = addCreatureReady(player1, new SlipstreamSerpent());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(serpent)));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    void cannotAttackWhenDefendingPlayerControlsNoIsland() {
        harness.addToBattlefield(player1, new Island());

        Permanent serpent = addCreatureReady(player1, new SlipstreamSerpent());
        int serpentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(serpent);

        assertThatThrownBy(() -> declareAttackers(List.of(serpentIndex)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBeCastFaceDownAndTurnedFaceUpForMorphCost() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new SlipstreamSerpent()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent serpent = findPermanent(player1, "Slipstream Serpent");
        assertThat(serpent.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.BLUE, 6);
        int serpentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(serpent);
        harness.turnFaceUp(player1, serpentIndex);
        harness.passBothPriorities();

        assertThat(serpent.isFaceDown()).isFalse();
    }

    @Test
    void faceDownSerpentSurvivesAndAttacksWithoutIslands() {
        harness.setHand(player1, List.of(new SlipstreamSerpent()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent serpent = findPermanent(player1, "Slipstream Serpent");
        serpent.setSummoningSick(false);
        harness.setLife(player2, 20);
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(serpent)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Slipstream Serpent");
        harness.assertLife(player2, 18);
    }

    @Test
    void turningFaceUpWithoutAnIslandTriggersSacrifice() {
        harness.setHand(player1, List.of(new SlipstreamSerpent()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent serpent = findPermanent(player1, "Slipstream Serpent");
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(serpent));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Slipstream Serpent");
        harness.assertInGraveyard(player1, "Slipstream Serpent");
    }

    @Test
    void gainingAnIslandAfterTheTriggerFiresDoesNotPreventSacrifice() {
        harness.castFromHand(player1, new SlipstreamSerpent(), "{7}{U}");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Slipstream Serpent");
        assertThat(gd.stack).hasSize(1);

        harness.addToBattlefield(player1, new Island());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Slipstream Serpent");
        harness.assertInGraveyard(player1, "Slipstream Serpent");
    }
}
