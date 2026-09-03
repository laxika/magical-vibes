package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IllvoiInfiltrator.class, GrizzlyBears.class, Shock.class, Forest.class})
class IllvoiInfiltratorTest extends BaseCardTest {

    @Test
    @DisplayName("Can be blocked before its controller casts two spells")
    void canBeBlockedBeforeTwoSpells() {
        castShocks(1);
        Permanent infiltrator = addCreatureReady(player1, new IllvoiInfiltrator());
        infiltrator.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(infiltrator))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cannot be blocked after its controller casts two spells")
    void cannotBeBlockedAfterTwoSpells() {
        Permanent infiltrator = addCreatureReady(player1, new IllvoiInfiltrator());
        infiltrator.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        castShocks(2);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(infiltrator)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Draws a card when it deals combat damage to a player")
    void drawsOnCombatDamageToPlayer() {
        addCreatureReady(player1, new IllvoiInfiltrator()).setAttacking(true);
        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    private void castShocks(int count) {
        List<Card> shocks = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            shocks.add(new Shock());
        }
        harness.setHand(player1, shocks);
        harness.addMana(player1, ManaColor.RED, count);
        for (int i = 0; i < count; i++) {
            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();
        }
    }
}
