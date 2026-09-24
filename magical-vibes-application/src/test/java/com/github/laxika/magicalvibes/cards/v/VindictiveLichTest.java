package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VindictiveLich.class, Forest.class, GrizzlyBears.class})
class VindictiveLichTest extends BaseCardTest {

    private static final String SACRIFICE_MODE = "Target opponent sacrifices a creature of their choice.";
    private static final String DISCARD_MODE = "Target opponent discards two cards.";
    private static final String LIFE_LOSS_MODE = "Target opponent loses 5 life.";

    @Test
    void sacrificeModeMakesTargetOpponentSacrificeACreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        addLichAndTriggerDeath();

        chooseMode(SACRIFICE_MODE);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void discardModeMakesTargetOpponentDiscardTwoCards() {
        harness.setHand(player2, List.of(new Forest(), new GrizzlyBears()));
        addLichAndTriggerDeath();

        chooseMode(DISCARD_MODE);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void lifeLossModeMakesTargetOpponentLoseFiveLife() {
        addLichAndTriggerDeath();

        chooseMode(LIFE_LOSS_MODE);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
    }

    @Test
    void modesCanOnlyTargetOpponents() {
        addLichAndTriggerDeath();
        harness.handleListChoice(player1, LIFE_LOSS_MODE);
        harness.handleListChoice(player1, ChooseOneEffect.FINISH_MODE_SELECTION);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addLichAndTriggerDeath() {
        Permanent lich = harness.addToBattlefieldAndReturn(player1, new VindictiveLich());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, lich));
        harness.passBothPriorities();
    }

    private void chooseMode(String mode) {
        harness.handleListChoice(player1, mode);
        harness.handleListChoice(player1, ChooseOneEffect.FINISH_MODE_SELECTION);
    }
}
