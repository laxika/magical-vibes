package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ValkyrieHarbinger.class)
class ValkyrieHarbingerTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 4/4 Angel token with flying and vigilance after gaining 4 life")
    void createsAngelTokenAtLifeThreshold() {
        harness.addToBattlefield(player1, new ValkyrieHarbinger());
        gd.lifeGainedThisTurn.put(player1.getId(), 4);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        Permanent angel = findPermanent(player1, "Angel");
        assertThat(angel.getCard().getPower()).isEqualTo(4);
        assertThat(angel.getCard().getToughness()).isEqualTo(4);
        assertThat(angel.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(angel.getCard().getSubtypes()).containsExactly(CardSubtype.ANGEL);
        assertThat(angel.getCard().isToken()).isTrue();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Creates no Angel token after gaining less than 4 life")
    void noTokenBelowLifeThreshold() {
        harness.addToBattlefield(player1, new ValkyrieHarbinger());
        gd.lifeGainedThisTurn.put(player1.getId(), 3);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Angel")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Triggers at the opponent's end step when its controller gained 4 life")
    void triggersOnOpponentEndStep() {
        harness.addToBattlefield(player1, new ValkyrieHarbinger());
        gd.lifeGainedThisTurn.put(player1.getId(), 4);

        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Angel")).hasSize(1);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }

    @Test
    @DisplayName("Separate life gains count even when subsequent life loss exceeds them")
    void accumulatedLifeGainIsNotNetLifeChange() {
        harness.addToBattlefield(player1, new ValkyrieHarbinger());
        harness.inMutationScope(() -> {
            harness.getLifeSupport().applyGainLife(gd, player1.getId(), 2);
            harness.getLifeSupport().applyGainLife(gd, player1.getId(), 2);
            harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), 6, "life loss");
        });

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Angel")).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's life gain does not qualify the controller")
    void opponentLifeGainDoesNotTrigger() {
        harness.addToBattlefield(player1, new ValkyrieHarbinger());
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 4));

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Angel")).isEmpty();
        assertThat(findPermanents(player2, "Angel")).isEmpty();
    }

    @Test
    @DisplayName("Gaining life after the end step begins does not trigger retroactively")
    void lateLifeGainDoesNotTrigger() {
        harness.addToBattlefield(player1, new ValkyrieHarbinger());

        advanceToEndStep(player1);
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 4));

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Angel")).isEmpty();
    }

    @Test
    @DisplayName("Life gained before Harbinger enters counts and excess life creates only one token")
    void earlierExcessLifeGainCreatesOneToken() {
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 12));
        harness.addToBattlefield(player1, new ValkyrieHarbinger());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Angel")).hasSize(1);
    }
}
