package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReleaseToTheWindTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a nonland permanent and lets its owner cast it for free")
    void exilesPermanentAndGrantsOwnerFreeCast() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        UUID bearsCardId = bears.getOriginalCard().getId();

        harness.setHand(player1, List.of(new ReleaseToTheWind()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(bearsCardId));
        assertThat(gd.exilePlayPermissions.get(bearsCardId)).isEqualTo(player2.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).contains(bearsCardId);
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(bearsCardId);
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd).doesNotContainKey(bearsCardId);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.castFromExile(player1, bearsCardId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No permission to play this exiled card");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player2, bearsCardId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(bearsCardId);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.setHand(player1, List.of(new ReleaseToTheWind()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland");
    }
}
