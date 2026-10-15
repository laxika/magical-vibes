package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MyrSire;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VannifarEvolvedEnigma.class, GrizzlyBears.class, MyrSire.class, Forest.class})
class VannifarEvolvedEnigmaTest extends BaseCardTest {

    @Test
    void cloaksACardFromHand() {
        harness.addToBattlefield(player1, new VannifarEvolvedEnigma());
        Card card = new GrizzlyBears();
        harness.setHand(player1, List.of(card));

        advanceToBeginningOfCombat(player1);
        harness.handleListChoice(player1, "Cloak a card from your hand");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent cloaked = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isCloaked)
                .findFirst()
                .orElseThrow();
        assertThat(cloaked.isFaceDown()).isTrue();
        assertThat(gqs.isCreature(gd, cloaked)).isTrue();
        assertThat(gqs.getEffectivePower(gd, cloaked)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cloaked)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void putsCountersOnColorlessCreaturesOnly() {
        harness.addToBattlefield(player1, new VannifarEvolvedEnigma());
        Permanent colorlessCreature = harness.addToBattlefieldAndReturn(player1, new MyrSire());
        Permanent coloredCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);
        harness.handleListChoice(player1, "Put a +1/+1 counter on each colorless creature you control");
        harness.passBothPriorities();

        assertThat(colorlessCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(coloredCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void canCloakALandButCannotTurnItFaceUpForItsManaCost() {
        harness.addToBattlefield(player1, new VannifarEvolvedEnigma());
        harness.setHand(player1, List.of(new Forest()));

        advanceToBeginningOfCombat(player1);
        harness.handleListChoice(player1, "Cloak a card from your hand");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent cloaked = gd.playerBattlefields.get(player1.getId()).get(1);
        assertThat(cloaked.isFaceDown()).isTrue();
        assertThat(gqs.isCreature(gd, cloaked)).isTrue();
        assertThat(gqs.getEffectivePower(gd, cloaked)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cloaked)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, cloaked, Keyword.WARD)).isTrue();
        assertThatThrownBy(() -> harness.turnFaceUp(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(cloaked.isFaceDown()).isTrue();
    }

    @Test
    void cloakedCreatureCanTurnFaceUpByPayingItsManaCost() {
        harness.addToBattlefield(player1, new VannifarEvolvedEnigma());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        advanceToBeginningOfCombat(player1);
        harness.handleListChoice(player1, "Cloak a card from your hand");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent cloaked = gd.playerBattlefields.get(player1.getId()).get(1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.turnFaceUp(player1, 1);

        assertThat(cloaked.isFaceDown()).isFalse();
        assertThat(gqs.hasKeyword(gd, cloaked, Keyword.WARD)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cloakModeWithEmptyHandResolvesWithoutAChoice() {
        harness.addToBattlefield(player1, new VannifarEvolvedEnigma());
        harness.setHand(player1, List.of());

        advanceToBeginningOfCombat(player1);
        harness.handleListChoice(player1, "Cloak a card from your hand");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerDuringOpponentsCombat() {
        harness.addToBattlefield(player1, new VannifarEvolvedEnigma());
        harness.setHand(player1, List.of(new Forest()));

        advanceToBeginningOfCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void counterModeIncludesCloakedCreaturesButExcludesLandsAndOpponentsCreatures() {
        harness.addToBattlefield(player1, new VannifarEvolvedEnigma());
        harness.setHand(player1, List.of(new Forest()));
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new MyrSire());

        advanceToBeginningOfCombat(player1);
        harness.handleListChoice(player1, "Cloak a card from your hand");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        Permanent cloaked = gd.playerBattlefields.get(player1.getId()).get(2);

        advanceToBeginningOfCombat(player1);
        harness.handleListChoice(player1, "Put a +1/+1 counter on each colorless creature you control");
        harness.passBothPriorities();

        assertThat(cloaked.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, cloaked)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cloaked)).isEqualTo(3);
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cloakedVannifarDoesNotHaveHerPrintedCombatTrigger() {
        Permanent vannifar = harness.addToBattlefieldAndReturn(player1, new VannifarEvolvedEnigma());
        vannifar.setFaceDownAsCloaked();
        harness.setHand(player1, List.of(new Forest()));

        advanceToBeginningOfCombat(player1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
