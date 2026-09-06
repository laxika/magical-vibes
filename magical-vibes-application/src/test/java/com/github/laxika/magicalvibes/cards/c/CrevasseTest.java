package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Crevasse.class, Mountain.class, Forest.class, GrizzlyBears.class})
class CrevasseTest extends BaseCardTest {

    @Test
    @DisplayName("Mountainwalk can be blocked while Crevasse is on the battlefield")
    void mountainwalkCanBeBlocked() {
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Crevasse());
        Permanent attacker = addWalker(player1, Keyword.MOUNTAINWALK);
        Permanent blocker = readyCreature(player2, new GrizzlyBears());

        beginBlockers();
        declareBlock(blocker, attacker);
    }

    @Test
    @DisplayName("Crevasse does not affect other landwalk abilities")
    void otherLandwalkRemainsUnblockable() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Crevasse());
        Permanent attacker = addWalker(player1, Keyword.FORESTWALK);
        Permanent blocker = readyCreature(player2, new GrizzlyBears());

        beginBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }

    private Permanent addWalker(Player player, Keyword landwalk) {
        Card card = new Card();
        card.setName("Test Walker");
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.GREEN);
        card.setPower(2);
        card.setToughness(2);
        card.setKeywords(EnumSet.of(landwalk));
        Permanent permanent = readyCreature(player, card);
        permanent.setAttacking(true);
        return permanent;
    }

    private Permanent readyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void beginBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
