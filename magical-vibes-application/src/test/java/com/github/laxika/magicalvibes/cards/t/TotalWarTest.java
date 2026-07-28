package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfGranite;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TotalWarTest extends BaseCardTest {

    private Permanent addCreature(Player owner, com.github.laxika.magicalvibes.model.Card card, boolean summoningSick) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(summoningSick);
        gd.playerBattlefields.get(owner.getId()).add(permanent);
        return permanent;
    }

    private void attackWith(Player attacker, List<Integer> indices) {
        harness.forceActivePlayer(attacker);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, attacker, indices);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }

    @Test
    @DisplayName("Destroys the attacking player's untapped non-attacking creatures")
    void destroysStayHomeCreatures() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new TotalWar()));
        Permanent attacker = addCreature(player2, new GrizzlyBears(), false);
        Permanent stayedHome = addCreature(player2, new GrizzlyBears(), false);

        attackWith(player2, List.of(0));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .contains(attacker.getId())
                .doesNotContain(stayedHome.getId());
    }

    @Test
    @DisplayName("Spares Walls, tapped creatures and creatures that arrived this turn")
    void sparesExemptCreatures() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new TotalWar()));
        addCreature(player2, new GrizzlyBears(), false);
        Permanent wall = addCreature(player2, new WallOfGranite(), false);
        Permanent tapped = addCreature(player2, new GrizzlyBears(), false);
        tapped.tap();
        Permanent freshlyArrived = addCreature(player2, new GrizzlyBears(), true);

        attackWith(player2, List.of(0));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .contains(wall.getId(), tapped.getId(), freshlyArrived.getId());
    }

    @Test
    @DisplayName("Leaves the non-attacking player's creatures alone")
    void ignoresNonAttackingPlayer() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new TotalWar()));
        Permanent defenderCreature = addCreature(player1, new GrizzlyBears(), false);
        addCreature(player2, new GrizzlyBears(), false);

        attackWith(player2, List.of(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .contains(defenderCreature.getId());
    }
}
