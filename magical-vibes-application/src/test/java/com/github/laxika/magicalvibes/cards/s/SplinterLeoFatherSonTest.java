package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SplinterLeoFatherSon.class, GrizzlyBears.class})
class SplinterLeoFatherSonTest extends BaseCardTest {

    private static final String TOKEN_MODE =
            "Target player creates a 2/2 red Mutant creature token.";
    private static final String COUNTER_MODE =
            "Put a +1/+1 counter on each other creature target player controls.";

    @Test
    void tokenModeCreatesTheTokenUnderTheTargetPlayersControl() {
        castSplinter();

        harness.handleListChoice(player1, TOKEN_MODE);
        harness.handleListChoice(player1, ChooseOneEffect.FINISH_MODE_SELECTION);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .contains("Mutant");
    }

    @Test
    void bothModesTargetDifferentPlayersAndCounterModeExcludesSplinter() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent splinter = castSplinter();

        harness.handleListChoice(player1, TOKEN_MODE);
        harness.handleListChoice(player1, COUNTER_MODE);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .contains("Mutant");
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(splinter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void bothModesCannotTargetTheSamePlayer() {
        castSplinter();

        harness.handleListChoice(player1, TOKEN_MODE);
        harness.handleListChoice(player1, COUNTER_MODE);
        harness.handlePermanentChosen(player1, player1.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent castSplinter() {
        harness.setHand(player1, List.of(new SplinterLeoFatherSon()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        return findPermanent(player1, "Splinter & Leo, Father & Son");
    }
}
