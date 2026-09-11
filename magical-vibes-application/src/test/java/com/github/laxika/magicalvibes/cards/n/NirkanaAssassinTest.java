package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NirkanaAssassin.class, FountainOfYouth.class})
class NirkanaAssassinTest extends BaseCardTest {

    @Test
    @DisplayName("Gains deathtouch when its controller gains life")
    void gainsDeathtouchOnControllerLifeGain() {
        Permanent assassin = harness.addToBattlefieldAndReturn(player1, new NirkanaAssassin());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(assassin.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Does not trigger when an opponent gains life")
    void doesNotTriggerOnOpponentLifeGain() {
        Permanent assassin = harness.addToBattlefieldAndReturn(player1, new NirkanaAssassin());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(assassin.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Deathtouch wears off at end of turn")
    void deathtouchWearsOffAtEndOfTurn() {
        Permanent assassin = harness.addToBattlefieldAndReturn(player1, new NirkanaAssassin());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(assassin.hasKeyword(Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(assassin.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
    }
}
