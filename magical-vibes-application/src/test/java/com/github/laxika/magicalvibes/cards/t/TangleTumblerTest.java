package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TangleTumbler.class, GrizzlyBears.class})
class TangleTumblerTest extends BaseCardTest {

    @Test
    void counterAbilityAddsCounterToTargetCreature() {
        Permanent tumbler = addTumbler(player1);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(tumbler.isTapped()).isTrue();
    }

    @Test
    void counterAbilityRejectsNonCreatureTarget() {
        Permanent tumbler = addTumbler(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, tumbler.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void tokenAbilityTapsExactlyTwoTokensAndAnimatesTumbler() {
        Permanent tumbler = addTumbler(player1);
        Permanent firstToken = addToken(player1, "First token");
        Permanent secondToken = addToken(player1, "Second token");
        Permanent ordinaryCreature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, firstToken.getId());
        harness.handlePermanentChosen(player1, secondToken.getId());
        harness.passBothPriorities();

        assertThat(firstToken.isTapped()).isTrue();
        assertThat(secondToken.isTapped()).isTrue();
        assertThat(ordinaryCreature.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, tumbler)).isTrue();
        assertThat(tumbler.isAnimatedUntilEndOfTurn()).isTrue();
    }

    @Test
    void tokenAbilityRequiresTwoTokens() {
        addTumbler(player1);
        addToken(player1, "Only token");
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents");
    }

    @Test
    void tokenAnimationEndsAtEndOfTurn() {
        Permanent tumbler = addTumbler(player1);
        Permanent firstToken = addToken(player1, "First token");
        Permanent secondToken = addToken(player1, "Second token");

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, firstToken.getId());
        harness.handlePermanentChosen(player1, secondToken.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(tumbler.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, tumbler)).isFalse();
    }

    private Permanent addTumbler(Player player) {
        Permanent tumbler = new Permanent(new TangleTumbler());
        tumbler.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(tumbler);
        return tumbler;
    }

    private Permanent addToken(Player player, String name) {
        Card tokenCard = new Card() {
        };
        tokenCard.setName(name);
        tokenCard.setType(CardType.CREATURE);
        tokenCard.setPower(1);
        tokenCard.setToughness(1);
        tokenCard.setToken(true);

        Permanent token = new Permanent(tokenCard);
        token.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(token);
        return token;
    }
}
