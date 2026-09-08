package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RiptideGearhulkTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a targeted nonland permanent third from the top of its owner's library")
    void putsTargetThirdFromTop() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castRiptideGearhulk(List.of(target.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId()).get(2).getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Can choose no targets")
    void canChooseNoTargets() {
        castRiptideGearhulk(List.of());

        harness.assertOnBattlefield(player1, "Riptide Gearhulk");
    }

    @Test
    @DisplayName("Cannot target a land or a permanent controlled by its controller")
    void cannotTargetLandOrOwnPermanent() {
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Plains());
        prepareCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(ownPermanent.getId())))
                .isInstanceOf(IllegalStateException.class);

        prepareCast();
        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(opponentLand.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castRiptideGearhulk(List<java.util.UUID> targetIds) {
        prepareCast();
        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new RiptideGearhulk()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
