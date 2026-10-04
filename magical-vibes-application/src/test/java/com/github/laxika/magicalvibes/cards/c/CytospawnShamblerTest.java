package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.m.MagewrightsStone;
import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.cards.s.Solemnity;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CytospawnShambler.class, MagewrightsStone.class, MistralCharger.class, Solemnity.class})
class CytospawnShamblerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with six +1/+1 counters")
    void entersWithSixCounters() {
        Permanent shambler = castShambler();

        assertThat(shambler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    @DisplayName("Graft moves a counter onto another creature that enters")
    void graftMovesCounterOntoEnteringCreature() {
        Permanent shambler = castShambler();

        harness.castFromHand(player1, new MistralCharger(), "{1}{W}");
        harness.passBothPriorities();
        Permanent charger = findPermanent(player1, "Mistral Charger");

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(shambler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Graft moves a counter onto an opponent's creature that enters")
    void graftMovesCounterOntoOpponentsEnteringCreature() {
        Permanent shambler = castShambler();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new MistralCharger(), "{1}{W}");
        harness.passBothPriorities();
        Permanent charger = findPermanent(player2, "Mistral Charger");

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(shambler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Graft may be declined")
    void graftMayBeDeclined() {
        Permanent shambler = castShambler();

        harness.castFromHand(player1, new MistralCharger(), "{1}{W}");
        harness.passBothPriorities();
        Permanent charger = findPermanent(player1, "Mistral Charger");

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(shambler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The activated ability grants trample until end of turn")
    void grantsTrampleUntilEndOfTurn() {
        Permanent charger = addCreatureReady(player1, new MistralCharger());
        charger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent shambler = castShambler();
        shambler.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(shambler),
                null, charger.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, charger, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, charger, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The activated ability can target an opponent's creature with a +1/+1 counter")
    void grantsTrampleToOpponentCreatureWithCounter() {
        Permanent charger = addCreatureReady(player2, new MistralCharger());
        charger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent shambler = castShambler();
        shambler.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(shambler),
                null, charger.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, charger, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The activated ability cannot target a creature without a +1/+1 counter")
    void cannotTargetCreatureWithoutCounter() {
        Permanent charger = addCreatureReady(player1, new MistralCharger());
        Permanent shambler = castShambler();
        shambler.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(shambler), null, charger.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The activated ability cannot target a noncreature permanent with a +1/+1 counter")
    void cannotTargetNoncreatureWithCounter() {
        Permanent stone = harness.addToBattlefieldAndReturn(player1, new MagewrightsStone());
        stone.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent shambler = castShambler();
        shambler.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(shambler), null, stone.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The activated ability fizzles if the target loses its +1/+1 counter before resolution")
    void targetMustStillHaveCounterOnResolution() {
        Permanent charger = addCreatureReady(player1, new MistralCharger());
        charger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent shambler = castShambler();
        shambler.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(shambler),
                null, charger.getId());
        charger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, charger, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @CardUsed(Solemnity.class)
    @DisplayName("Graft leaves the source counter in place when counters cannot be put on the entering creature")
    void graftCannotMoveCounterUnderSolemnity() {
        Permanent shambler = castShambler();

        harness.castFromHand(player1, new Solemnity(), "{2}{W}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new MistralCharger(), "{1}{W}");
        harness.passBothPriorities();
        Permanent charger = findPermanent(player1, "Mistral Charger");

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(shambler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Graft leaves the source counter in place if the entering creature leaves before resolution")
    void graftCannotMoveCounterToDepartedCreature() {
        Permanent shambler = castShambler();

        harness.castFromHand(player1, new MistralCharger(), "{1}{W}");
        harness.passBothPriorities();
        Permanent charger = findPermanent(player1, "Mistral Charger");

        harness.handleMayAbilityChosen(player1, true);
        gd.playerBattlefields.get(player1.getId()).remove(charger);
        gd.playerHands.get(player1.getId()).add(charger.getCard());
        harness.passBothPriorities();

        assertThat(shambler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The activated ability can target the tapped, summoning-sick Shambler itself")
    void canGrantTrampleToItselfWhileTappedAndSummoningSick() {
        Permanent shambler = castShambler();
        shambler.tap();
        assertThat(shambler.isSummoningSick()).isTrue();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(shambler),
                null, shambler.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, shambler, Keyword.TRAMPLE)).isTrue();
        assertThat(shambler.isTapped()).isTrue();
        assertThat(shambler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    @DisplayName("Trample remains after the target loses its counter following resolution")
    void trampleRemainsAfterCounterIsRemoved() {
        Permanent charger = addCreatureReady(player1, new MistralCharger());
        charger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent shambler = castShambler();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(shambler),
                null, charger.getId());
        harness.passBothPriorities();
        charger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThat(gqs.hasKeyword(gd, charger, Keyword.TRAMPLE)).isTrue();
    }

    private Permanent castShambler() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new CytospawnShambler(), "{6}{G}");
        harness.passBothPriorities();
        return findPermanent(player1, "Cytospawn Shambler");
    }
}
