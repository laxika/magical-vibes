package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SaprazzanOutriggerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts Saprazzan Outrigger on top of its owner's library at end of combat")
    void attackingPutsItOnTopOfLibrary() {
        Card outriggerCard = new SaprazzanOutrigger();
        Permanent outrigger = addReady(player1, outriggerCard);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getId().equals(outrigger.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).first().isSameAs(outriggerCard);
    }

    @Test
    @DisplayName("Blocking puts Saprazzan Outrigger on top of its owner's library at end of combat")
    void blockingPutsItOnTopOfLibrary() {
        Permanent attacker = addReady(player1, new GiantSpider());
        attacker.setAttacking(true);
        Card outriggerCard = new SaprazzanOutrigger();
        Permanent outrigger = addReady(player2, outriggerCard);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(
                permanent -> permanent.getId().equals(outrigger.getId()));
        assertThat(gd.playerDecks.get(player2.getId())).first().isSameAs(outriggerCard);
    }

    @Test
    @DisplayName("The end-of-combat move is skipped if Saprazzan Outrigger leaves first")
    void doesNotMoveIfItLeavesBeforeEndOfCombat() {
        Card outriggerCard = new SaprazzanOutrigger();
        Permanent outrigger = addReady(player1, outriggerCard);

        declareAttackers(List.of(0));
        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getId().equals(outrigger.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(outriggerCard);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
