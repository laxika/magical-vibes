package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AyumiTheLastVisitorTest extends BaseCardTest {

    @Test
    @DisplayName("Ayumi can't be blocked when defending player controls a legendary land")
    void cannotBeBlockedWhenDefenderControlsLegendaryLand() {
        Permanent legendaryForest = new Permanent(new Forest());
        TestCards.mutableCard(legendaryForest).setSupertypes(
                EnumSet.of(CardSupertype.BASIC, CardSupertype.LEGENDARY));
        gd.playerBattlefields.get(player2.getId()).add(legendaryForest);
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        Permanent ayumi = addReadyAttacker(player1);

        prepareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, ayumi))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Ayumi can be blocked when defending player controls a nonlegendary land")
    void canBeBlockedWhenDefenderControlsNonlegendaryLand() {
        harness.addToBattlefield(player2, new Forest());
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        Permanent ayumi = addReadyAttacker(player1);

        prepareBlockers();
        declareBlock(blocker, ayumi);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Ayumi can be blocked when defending player controls a legendary nonland")
    void canBeBlockedWhenDefenderControlsLegendaryNonland() {
        Permanent legendaryCreature = new Permanent(new GrizzlyBears());
        TestCards.mutableCard(legendaryCreature).setSupertypes(EnumSet.of(CardSupertype.LEGENDARY));
        gd.playerBattlefields.get(player2.getId()).add(legendaryCreature);
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        Permanent ayumi = addReadyAttacker(player1);

        prepareBlockers();
        declareBlock(blocker, ayumi);

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addReadyAttacker(Player player) {
        Permanent attacker = new Permanent(new AyumiTheLastVisitor());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(attacker);
        return attacker;
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent creature = new Permanent(card);
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }

    private void prepareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
