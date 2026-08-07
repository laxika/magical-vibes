package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlmsTest extends BaseCardTest {

    private void addAlmsReady() {
        harness.addToBattlefield(player1, new Alms());
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    @Test
    @DisplayName("Shields the target creature for 1 and exiles the top card of the graveyard")
    void shieldsTargetAndExilesTopGraveyardCard() {
        addAlmsReady();
        harness.setGraveyard(player1, List.of(new Plains(), new Forest()));
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getDamagePreventionShield()).isEqualTo(1);

        // The most recently added card (the last of the list) is the top of the graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Plains");
        assertThat(gd.exiledCards)
                .extracting(e -> e.card().getName())
                .contains("Forest");
    }

    @Test
    @DisplayName("Cannot be activated with an empty graveyard")
    void requiresNonEmptyGraveyard() {
        addAlmsReady();
        harness.setGraveyard(player1, List.of());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        addAlmsReady();
        harness.setGraveyard(player1, List.of(new Plains()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
