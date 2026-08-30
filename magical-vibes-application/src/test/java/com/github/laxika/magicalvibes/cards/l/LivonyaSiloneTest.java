package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LivonyaSilone.class, Forest.class, GrizzlyBears.class})
class LivonyaSiloneTest extends BaseCardTest {

    @Test
    @DisplayName("Livonya Silone can't be blocked when defending player controls a legendary land")
    void cannotBeBlockedWhenDefenderControlsLegendaryLand() {
        Permanent legendaryForest = new Permanent(new Forest());
        TestCards.mutableCard(legendaryForest).setSupertypes(
                EnumSet.of(CardSupertype.BASIC, CardSupertype.LEGENDARY));
        gd.playerBattlefields.get(player2.getId()).add(legendaryForest);
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        Permanent livonya = addReadyAttacker(player1);

        prepareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, livonya))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Livonya Silone can be blocked when defending player controls a nonlegendary land")
    void canBeBlockedWhenDefenderControlsNonlegendaryLand() {
        harness.addToBattlefield(player2, new Forest());
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        Permanent livonya = addReadyAttacker(player1);

        prepareBlockers();
        declareBlock(blocker, livonya);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Livonya Silone can be blocked when defending player controls a legendary nonland")
    void canBeBlockedWhenDefenderControlsLegendaryNonland() {
        Permanent legendaryCreature = new Permanent(new GrizzlyBears());
        TestCards.mutableCard(legendaryCreature).setSupertypes(EnumSet.of(CardSupertype.LEGENDARY));
        gd.playerBattlefields.get(player2.getId()).add(legendaryCreature);
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        Permanent livonya = addReadyAttacker(player1);

        prepareBlockers();
        declareBlock(blocker, livonya);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("First strike lets Livonya Silone survive combat with an equal blocker")
    void firstStrikeLetsLivonyaSurviveEqualCombat() {
        LivonyaSilone card = new LivonyaSilone();
        card.setPower(2);
        card.setToughness(2);
        Permanent livonya = addReadyAttacker(player1, card);
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Livonya Silone");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    private Permanent addReadyAttacker(Player player) {
        return addReadyAttacker(player, new LivonyaSilone());
    }

    private Permanent addReadyAttacker(Player player, Card card) {
        Permanent attacker = new Permanent(card);
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
