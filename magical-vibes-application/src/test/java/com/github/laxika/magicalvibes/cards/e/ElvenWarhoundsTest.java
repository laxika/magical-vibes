package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ElvenWarhoundsTest extends BaseCardTest {

    @Test
    @DisplayName("The blocking creature is put on top of its owner's library")
    void tucksBlocker() {
        addAttackingWarhounds();
        Permanent blocker = addBlocker(new GrizzlyBears());

        block();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(blocker.getCard());
    }

    @Test
    @DisplayName("Every blocker is tucked when multiple creatures block")
    void tucksEveryBlocker() {
        addAttackingWarhounds();
        Permanent bears = addBlocker(new GrizzlyBears());
        Permanent giant = addBlocker(new HillGiant());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId()).subList(0, 2))
                .containsExactlyInAnyOrder(bears.getCard(), giant.getCard());
    }

    @Test
    @DisplayName("Nothing is tucked when the Warhounds attack unblocked")
    void unblockedTucksNothing() {
        addAttackingWarhounds();
        Permanent bystander = addBlocker(new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bystander);
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(bystander.getCard());
    }

    private void addAttackingWarhounds() {
        Permanent hounds = new Permanent(new ElvenWarhounds());
        hounds.setSummoningSick(false);
        hounds.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(hounds);
    }

    private Permanent addBlocker(Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(permanent);
        return permanent;
    }

    private void block() {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();
    }
}
