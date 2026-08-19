package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MagnigothTreefolkTest extends BaseCardTest {

    @Test
    @DisplayName("Gains landwalk for a basic land type among its controller's lands")
    void gainsLandwalkForControlledBasicLandType() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        Permanent blocker = addReadyCreature(player2);
        Permanent treefolk = addAttacker(player1);

        beginBlockers();

        assertThatThrownBy(() -> declareBlocker(blocker, treefolk))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Only the land types controlled by the Treefolk's controller count")
    void opponentLandTypesDoNotCountForDomain() {
        harness.addToBattlefield(player2, new Forest());
        Permanent blocker = addReadyCreature(player2);
        Permanent treefolk = addAttacker(player1);

        beginBlockers();

        declareBlocker(blocker, treefolk);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A nonland permanent with a basic land subtype does not count")
    void nonlandBasicSubtypeDoesNotCount() {
        Permanent forestCreature = addReadyCreature(player1);
        TestCards.mutableCard(forestCreature).setSubtypes(List.of(CardSubtype.FOREST));
        harness.addToBattlefield(player2, new Forest());
        Permanent blocker = addReadyCreature(player2);
        Permanent treefolk = addAttacker(player1);

        beginBlockers();

        declareBlocker(blocker, treefolk);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Each controlled basic land type grants its corresponding landwalk")
    void eachControlledBasicLandTypeGrantsItsOwnLandwalk() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        Permanent blocker = addReadyCreature(player2);
        Permanent treefolk = addAttacker(player1);

        beginBlockers();

        assertThatThrownBy(() -> declareBlocker(blocker, treefolk))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    private Permanent addReadyCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private Permanent addAttacker(Player player) {
        Permanent treefolk = new Permanent(new MagnigothTreefolk());
        treefolk.setSummoningSick(false);
        treefolk.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(treefolk);
        return treefolk;
    }

    private void beginBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    private void declareBlocker(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }
}
