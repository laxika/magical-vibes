package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SauroformHybrid;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BiogenicOoze.class, SauroformHybrid.class})
class BiogenicOozeTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a 2/2 green Ooze token")
    void entersAndCreatesOozeToken() {
        castBiogenicOoze();

        Permanent token = findPermanent(player1, "Ooze");
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.OOZE);
    }

    @Test
    @DisplayName("End step puts a +1/+1 counter on each Ooze you control")
    void endStepCountersOwnOozesOnly() {
        castBiogenicOoze();
        Permanent source = findPermanent(player1, "Biogenic Ooze");
        Permanent token = findPermanent(player1, "Ooze");
        Permanent ownNonOoze = addCreatureReady(player1, new SauroformHybrid());
        Permanent opponentOoze = addCreatureReady(player2, new BiogenicOoze());

        runToEndStep();

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownNonOoze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentOoze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("{1}{G}{G}{G} creates a 2/2 green Ooze token")
    void activatedAbilityCreatesOozeToken() {
        Permanent source = addCreatureReady(player1, new BiogenicOoze());
        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, sourceIndex, 0, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Ooze")).hasSize(1);
    }

    @Test
    void tappedSummoningSickOozeCanActivateRepeatedly() {
        castBiogenicOoze();
        Permanent source = findPermanent(player1, "Biogenic Ooze");
        source.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 6);
        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(source);

        harness.activateAbility(player1, sourceIndex, 0, null);
        resolveAllTriggers();
        harness.activateAbility(player1, sourceIndex, 0, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Ooze")).hasSize(3).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(2);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.OOZE);
        });
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        castBiogenicOoze();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Biogenic Ooze")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player1, "Ooze")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void multipleBiogenicOozesEachPutCountersOnEveryControlledOoze() {
        castBiogenicOoze();
        addCreatureReady(player1, new BiogenicOoze());

        runToEndStep();

        assertThat(findPermanents(player1, "Biogenic Ooze")).hasSize(2)
                .allSatisfy(ooze -> assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isEqualTo(2));
        assertThat(findPermanent(player1, "Ooze")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void tokenCreatedInResponseToEndStepTriggerAlsoGetsCounter() {
        castBiogenicOoze();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 3);
        Permanent source = findPermanent(player1, "Biogenic Ooze");
        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(source);

        harness.activateAbility(player1, sourceIndex, 0, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Ooze")).hasSize(2)
                .allSatisfy(ooze -> assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isEqualTo(1));
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void castBiogenicOoze() {
        harness.setHand(player1, List.of(new BiogenicOoze()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    private void runToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
