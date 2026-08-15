package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MournwillowTest extends BaseCardTest {

    @Test
    @DisplayName("With delirium, a creature with power 2 or less cannot block this turn")
    void deliriumPreventsSmallCreatureFromBlocking() {
        Permanent attacker = addCreatureReady(player1, new HillGiant());
        addCreatureReady(player2, new GrizzlyBears());
        castMournwillow(List.of(new GrizzlyBears(), new Shock(), new Divination(), new Forest()));

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("With delirium, a creature with power 3 can still block")
    void deliriumDoesNotPreventLargeCreatureFromBlocking() {
        Permanent attacker = addCreatureReady(player1, new HillGiant());
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        castMournwillow(List.of(new GrizzlyBears(), new Shock(), new Divination(), new Forest()));

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker), 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Without delirium, a creature with power 2 can block")
    void withoutDeliriumDoesNotPreventBlocking() {
        Permanent attacker = addCreatureReady(player1, new HillGiant());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        castMournwillow(List.of(new GrizzlyBears(), new Shock(), new Forest()));

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker), 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void castMournwillow(List<Card> graveyard) {
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new Mournwillow()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
