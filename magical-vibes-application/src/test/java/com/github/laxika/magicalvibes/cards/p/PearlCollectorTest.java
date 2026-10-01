package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({PearlCollector.class, GrizzlyBears.class})
class PearlCollectorTest extends BaseCardTest {

    @Test
    @DisplayName("Conjures Mox Pearl after gaining at least four life this turn")
    void conjuresMoxPearlAfterGainingFourLife() {
        addPearlCollector();
        harness.setHand(player1, java.util.List.of());
        gd.lifeGainedThisTurn.put(player1.getId(), 4);

        enterPostcombatMain();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Mox Pearl");
    }

    @Test
    @DisplayName("Does not conjure Mox Pearl after gaining less than four life")
    void doesNotConjureMoxPearlAfterGainingLessThanFourLife() {
        addPearlCollector();
        gd.lifeGainedThisTurn.put(player1.getId(), 3);

        enterPostcombatMain();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .doesNotContain("Mox Pearl");
    }

    @Test
    @DisplayName("The conjure trigger works only once for the permanent")
    void conjureTriggersOnlyOnce() {
        addPearlCollector();
        harness.setHand(player1, java.util.List.of());
        harness.setHand(player2, java.util.List.of());
        gd.lifeGainedThisTurn.put(player1.getId(), 4);

        enterPostcombatMain();
        resolveAllTriggers();

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        gd.lifeGainedThisTurn.put(player1.getId(), 4);
        enterPostcombatMain();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).filteredOn(Card::getName, "Mox Pearl").hasSize(1);
    }

    @Test
    @DisplayName("Another creature you control perpetually gains lifelink")
    void anotherCreatureYouControlPerpetuallyGainsLifelink() {
        addPearlCollector();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void cannotTargetPearlCollectorItself() {
        Permanent pearlCollector = addPearlCollector();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, pearlCollector.getId()))
                .hasMessageContaining("another creature you control");
    }

    private Permanent addPearlCollector() {
        return addCreatureReady(player1, new PearlCollector());
    }

    private void enterPostcombatMain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
