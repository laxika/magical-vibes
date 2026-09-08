package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KrovikanRotTest extends BaseCardTest {

    private void giveKrovikanRot() {
        harness.setHand(player1, List.of(new KrovikanRot()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    @Test
    void destroysCreatureWithPowerTwoOrLess() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        giveKrovikanRot();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void cannotTargetCreatureWithPowerGreaterThanTwo() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        giveKrovikanRot();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 2 or less");
    }

    @Test
    void recoverReturnsKrovikanRotToHandWhenPaid() {
        Card rot = new KrovikanRot();
        harness.setGraveyard(player1, List.of(rot));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(rot);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(rot);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(rot);
    }

    @Test
    void recoverExilesKrovikanRotWhenDeclined() {
        Card rot = new KrovikanRot();
        harness.setGraveyard(player1, List.of(rot));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(rot);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(rot);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(rot);
    }
}
