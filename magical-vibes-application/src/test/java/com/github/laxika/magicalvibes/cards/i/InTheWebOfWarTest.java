package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InTheWebOfWar.class, GnarledMass.class})
class InTheWebOfWarTest extends BaseCardTest {

    @Test
    @DisplayName("A creature you control entering gets +2/+0 and haste")
    void boostsAndHastesEnteringCreature() {
        harness.addToBattlefield(player1, new InTheWebOfWar());

        harness.castFromHand(player1, new GnarledMass(), "{1}{G}{G}");
        harness.passBothPriorities(); // resolve the creature spell -> it enters, trigger queues
        harness.passBothPriorities(); // resolve the trigger

        assertThat(gd.stack).isEmpty();
        Permanent mass = findPermanent(player1, "Gnarled Mass");
        assertThat(gqs.getEffectivePower(gd, mass)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, mass)).isEqualTo(3);
        assertThat(mass.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The boost and haste wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new InTheWebOfWar());

        harness.castFromHand(player1, new GnarledMass(), "{1}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent mass = findPermanent(player1, "Gnarled Mass");
        assertThat(gqs.getEffectivePower(gd, mass)).isEqualTo(3);
        assertThat(mass.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Triggers separately for each creature you control that enters")
    void triggersForEachEnteringCreature() {
        harness.addToBattlefield(player1, new InTheWebOfWar());

        harness.castFromHand(player1, new GnarledMass(), "{1}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castFromHand(player1, new GnarledMass(), "{1}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Gnarled Mass")).hasSize(2)
                .allSatisfy(mass -> {
                    assertThat(gqs.getEffectivePower(gd, mass)).isEqualTo(5);
                    assertThat(gqs.getEffectiveToughness(gd, mass)).isEqualTo(3);
                    assertThat(mass.hasKeyword(Keyword.HASTE)).isTrue();
                });
    }

    @Test
    @DisplayName("Does not trigger for a creature an opponent controls")
    void noTriggerForOpponentCreature() {
        harness.addToBattlefield(player1, new InTheWebOfWar());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new GnarledMass(), "{1}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent mass = findPermanent(player2, "Gnarled Mass");
        assertThat(gqs.getEffectivePower(gd, mass)).isEqualTo(3);
        assertThat(mass.hasKeyword(Keyword.HASTE)).isFalse();
    }
}
