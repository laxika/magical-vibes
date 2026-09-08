package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HellsThunder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MeticulousExcavationTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target permanent you control to its owner's hand")
    void returnsTargetPermanentToHand() {
        harness.addToBattlefield(player1, new MeticulousExcavation());
        HellsThunder targetCard = new HellsThunder();
        Permanent target = harness.addToBattlefieldAndReturn(player1, targetCard);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hell's Thunder");
        harness.assertInHand(player1, "Hell's Thunder");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(targetCard.getId()));
    }

    @Test
    @DisplayName("Exiles an unearthed permanent before returning it to its owner's hand")
    void exilesUnearthedPermanentBeforeReturningItToHand() {
        harness.addToBattlefield(player1, new MeticulousExcavation());
        HellsThunder targetCard = new HellsThunder();
        harness.setGraveyard(player1, List.of(targetCard));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent target = findPermanent(player1, "Hell's Thunder");
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hell's Thunder");
        harness.assertInHand(player1, "Hell's Thunder");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(targetCard.getId()));
    }

    @Test
    @DisplayName("Can target only a permanent you control")
    void canTargetOnlyOwnPermanent() {
        harness.addToBattlefield(player1, new MeticulousExcavation());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be activated only during its controller's turn")
    void canBeActivatedOnlyDuringItsControllersTurn() {
        harness.addToBattlefield(player1, new MeticulousExcavation());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
