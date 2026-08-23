package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlipstreamSerpent.class, Island.class})
class SlipstreamSerpentTest extends BaseCardTest {

    @Test
    void isSacrificedWhenControllerControlsNoIslands() {
        harness.setHand(player1, List.of(new SlipstreamSerpent()));
        harness.addMana(player1, ManaColor.BLUE, 8);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Slipstream Serpent");
        harness.assertInGraveyard(player1, "Slipstream Serpent");
    }

    @Test
    void survivesWhileControllerControlsAnIsland() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new SlipstreamSerpent()));
        harness.addMana(player1, ManaColor.BLUE, 8);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Slipstream Serpent");
    }

    @Test
    void canAttackWhenDefendingPlayerControlsAnIsland() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());

        Permanent serpent = addReadySerpent();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(serpent)));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    void cannotAttackWhenDefendingPlayerControlsNoIsland() {
        harness.addToBattlefield(player1, new Island());

        Permanent serpent = addReadySerpent();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(serpent))))
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

    private Permanent addReadySerpent() {
        Permanent serpent = new Permanent(new SlipstreamSerpent());
        serpent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(serpent);
        return serpent;
    }
}
