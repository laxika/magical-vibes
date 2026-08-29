package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AngelsMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpansionTest extends BaseCardTest {

    @Test
    @DisplayName("Expansion copies an instant or sorcery spell with mana value 4 or less")
    void expansionCopiesSmallInstantOrSorcery() {
        AngelsMercy mercy = new AngelsMercy();
        harness.setHand(player1, List.of(mercy, new Expansion()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.castInstant(player1, 0, 0, mercy.getId());
        harness.passBothPriorities();

        StackEntry copy = gd.stack.getLast();
        assertThat(copy.getDescription()).isEqualTo("Copy of Angel's Mercy");
        assertThat(copy.isCopy()).isTrue();
    }

    @Test
    @DisplayName("Expansion cannot target an instant or sorcery spell with mana value greater than 4")
    void expansionRejectsLargeInstantOrSorcery() {
        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player1, List.of(lavaAxe, new Expansion()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, lavaAxe.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Explosion deals X damage to one target and makes another player draw X cards")
    void explosionUsesBothTargetsAndPaidX() {
        harness.setHand(player1, List.of(new Expansion()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        gs.playModalXCard(gd, player1, 0, 1, 3, null, List.of(player2.getId(), player1.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore - 1 + 3);
    }

    @Test
    @DisplayName("Explosion requires a player for its card-draw target")
    void explosionCannotUsePermanentForCardDrawTarget() {
        harness.setHand(player1, List.of(new Expansion()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        var creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> gs.playModalXCard(
                gd, player1, 0, 1, 3, null, List.of(player2.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
