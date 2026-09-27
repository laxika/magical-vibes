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
        harness.passBothPriorities();
    }
}
