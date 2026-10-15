package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BarkformHarvester;
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

@CardUsed({TangleTumbler.class, BarkformHarvester.class})
class TangleTumblerTest extends BaseCardTest {

    @Test
    void counterAbilityAddsCounterToTargetCreature() {
        Permanent tumbler = addTumbler(player1);
        Permanent target = addCreatureReady(player1, new BarkformHarvester());
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
        Permanent ordinaryCreature = addCreatureReady(player1, new BarkformHarvester());

        harness.activateAbility(player1, 0, 1, null, null);
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
        addCreatureReady(player1, new BarkformHarvester());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents");
    }

    @Test
    void tokenAnimationEndsAtEndOfTurn() {
        Permanent tumbler = addTumbler(player1);
        addToken(player1, "First token");
        addToken(player1, "Second token");

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(tumbler.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, tumbler)).isFalse();
    }

    @Test
    void counterAbilityCanTargetOpponentsCreature() {
        addTumbler(player1);
        Permanent target = addCreatureReady(player2, new BarkformHarvester());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void counterAbilityRequiresThreeMana() {
        Permanent tumbler = addTumbler(player1);
        Permanent target = addCreatureReady(player1, new BarkformHarvester());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(tumbler.isTapped()).isFalse();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void tokenAbilityAcceptsSummoningSickCreatureTokens() {
        Permanent tumbler = addTumbler(player1);
        Permanent firstToken = addToken(player1, "First token");
        Permanent secondToken = addToken(player1, "Second token");
        firstToken.setSummoningSick(true);
        secondToken.setSummoningSick(true);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(firstToken.isTapped()).isTrue();
        assertThat(secondToken.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, tumbler)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, tumbler)).isTrue();
        assertThat(tumbler.isTapped()).isFalse();
    }

    @Test
    void tokenAbilityAcceptsNonCreatureTokensWhileTumblerIsTapped() {
        Permanent tumbler = addTumbler(player1);
        tumbler.tap();
        Permanent firstToken = addToken(player1, "First artifact token", CardType.ARTIFACT);
        Permanent secondToken = addToken(player1, "Second artifact token", CardType.ARTIFACT);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(firstToken.isTapped()).isTrue();
        assertThat(secondToken.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, tumbler)).isTrue();
        assertThat(tumbler.isTapped()).isTrue();
    }

    @Test
    void tokenAbilityRejectsTappedTokens() {
        Permanent tumbler = addTumbler(player1);
        Permanent untappedToken = addToken(player1, "Untapped token");
        Permanent tappedToken = addToken(player1, "Tapped token");
        tappedToken.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents");

        assertThat(untappedToken.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, tumbler)).isFalse();
    }

    @Test
    void tokenAbilityCannotUseOpponentsTokens() {
        Permanent tumbler = addTumbler(player1);
        Permanent ownToken = addToken(player1, "Own token");
        Permanent opposingToken = addToken(player2, "Opposing token");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents");

        assertThat(ownToken.isTapped()).isFalse();
        assertThat(opposingToken.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, tumbler)).isFalse();
    }

    @Test
    void animatedTumblerCanPutCounterOnItself() {
        Permanent tumbler = addTumbler(player1);
        addToken(player1, "First token");
        addToken(player1, "Second token");

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 0, null, tumbler.getId());
        harness.passBothPriorities();

        assertThat(tumbler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, tumbler)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, tumbler)).isEqualTo(7);
        assertThat(tumbler.isTapped()).isTrue();
    }

    @Test
    void counterAbilityRejectsTappedTumbler() {
        Permanent tumbler = addTumbler(player1);
        tumbler.tap();
        Permanent target = addCreatureReady(player1, new BarkformHarvester());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void newlyControlledNonCreatureTumblerCanUseTapAbility() {
        Permanent tumbler = addTumbler(player1);
        tumbler.setSummoningSick(true);
        Permanent target = addCreatureReady(player1, new BarkformHarvester());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(tumbler.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void newlyControlledAnimatedTumblerCannotUseTapAbility() {
        Permanent tumbler = addTumbler(player1);
        tumbler.setSummoningSick(true);
        addToken(player1, "First token");
        addToken(player1, "Second token");
        Permanent target = addCreatureReady(player1, new BarkformHarvester());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThat(gqs.isCreature(gd, tumbler)).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(tumbler.isTapped()).isFalse();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addTumbler(Player player) {
        return addCreatureReady(player, new TangleTumbler());
    }

    private Permanent addToken(Player player, String name) {
        return addToken(player, name, CardType.CREATURE);
    }

    private Permanent addToken(Player player, String name, CardType type) {
        Card tokenCard = new Card() {
        };
        tokenCard.setName(name);
        tokenCard.setType(type);
        tokenCard.setPower(1);
        tokenCard.setToughness(1);
        tokenCard.setToken(true);

        return addCreatureReady(player, tokenCard);
    }
}
