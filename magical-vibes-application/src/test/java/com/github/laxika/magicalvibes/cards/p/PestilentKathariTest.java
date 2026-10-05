package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PestilentKathari.class})
class PestilentKathariTest extends BaseCardTest {

    @Test
    @DisplayName("Activation works while tapped and summoning sick and affects only its source")
    void activatesWhileTappedAndSummoningSick() {
        Permanent kathari = harness.addToBattlefieldAndReturn(player1, new PestilentKathari());
        kathari.setSummoningSick(true);
        kathari.setTapped(true);
        Permanent other = addCreatureReady(player1, new PestilentKathari());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gqs.hasKeyword(gd, kathari, Keyword.FIRST_STRIKE)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, kathari, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(kathari.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Three generic mana cannot pay the red activation requirement")
    void activationRequiresRedMana() {
        Permanent kathari = addCreatureReady(player1, new PestilentKathari());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, kathari, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("First strike kills a flying deathtouch blocker before it can deal damage")
    void firstStrikePreventsDeathtouchRetaliation() {
        addCreatureReady(player1, new PestilentKathari());
        Permanent blocker = addCreatureReady(player2, new PestilentKathari());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Pestilent Kathari");
        harness.assertInGraveyard(player2, "Pestilent Kathari");
        harness.assertNotOnBattlefield(player2, "Pestilent Kathari");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("{2}{R} grants first strike until end of turn")
    void grantsFirstStrike() {
        Permanent kathari = addCreatureReady(player1, new PestilentKathari());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(kathari.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("First strike wears off at end of turn")
    void firstStrikeWearsOffAtEndOfTurn() {
        Permanent kathari = addCreatureReady(player1, new PestilentKathari());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(kathari.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(kathari.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE);
    }
}
