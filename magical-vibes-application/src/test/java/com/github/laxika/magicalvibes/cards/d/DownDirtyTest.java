package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DownDirtyTest extends BaseCardTest {

    private static final int DOWN = 0;
    private static final int DIRTY = 1;
    private static final int FUSE = 2;

    @Test
    @DisplayName("Down makes the targeted player discard two cards")
    void downDiscardsTwoCards() {
        harness.setHand(player2, List.of(new Plains(), new Island(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new DownDirty()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, DOWN, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Dirty returns any card from your graveyard to your hand")
    void dirtyReturnsTargetCard() {
        Plains target = new Plains();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new DownDirty()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, DIRTY, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Fuse resolves Down before Dirty")
    void fuseResolvesBothHalves() {
        harness.setHand(player2, List.of(new Plains(), new Island(), new GrizzlyBears()));
        Plains target = new Plains();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new DownDirty()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        gs.playCard(gd, player1, 0, FUSE, target.getId(), null,
                List.of(player2.getId(), target.getId()), List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).contains(target);
    }

    @Test
    @DisplayName("Down cannot target a permanent")
    void downCannotTargetPermanent() {
        UUID permanentId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new DownDirty()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, DOWN, permanentId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Dirty cannot target an opponent's graveyard")
    void dirtyCannotTargetOpponentsGraveyard() {
        Plains target = new Plains();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new DownDirty()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, DIRTY, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
