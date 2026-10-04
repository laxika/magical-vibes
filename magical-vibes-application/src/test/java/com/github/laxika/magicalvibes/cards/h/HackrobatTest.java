package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Hackrobat.class})
class HackrobatTest extends BaseCardTest {

    @Test
    void castsNormallyWithoutOpponentLifeLoss() {
        harness.setHand(player1, List.of(new Hackrobat()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hackrobat");
        harness.assertNotInHand(player1, "Hackrobat");
    }

    @Test
    void castsForSpectacleAfterOpponentLostLife() {
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        harness.setHand(player1, List.of(new Hackrobat()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hackrobat");
        harness.assertNotInHand(player1, "Hackrobat");
    }

    @Test
    void spectacleUnavailableWithoutOpponentLifeLoss() {
        harness.setHand(player1, List.of(new Hackrobat()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Hackrobat");
    }

    @Test
    void controllersLifeLossDoesNotEnableSpectacle() {
        gd.lifeLostThisTurn.put(player1.getId(), 1);
        harness.setHand(player1, List.of(new Hackrobat()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Hackrobat");
    }

    @Test
    void repeatedRedAbilityPutsHackrobatIntoGraveyard() {
        addHackrobat(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hackrobat");
        harness.assertInGraveyard(player1, "Hackrobat");
    }

    @Test
    void abilitiesCanBeActivatedWhileSummoningSick() {
        Permanent hackrobat = harness.addToBattlefieldAndReturn(player1, new Hackrobat());
        hackrobat.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, hackrobat, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.getEffectivePower(gd, hackrobat)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hackrobat)).isEqualTo(1);
    }

    @Test
    @DisplayName("The black ability grants deathtouch until end of turn")
    void grantsDeathtouch() {
        Permanent hackrobat = addHackrobat(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, hackrobat, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("The red ability gives Hackrobat +2/-2 until end of turn")
    void boostsPowerAndReducesToughness() {
        Permanent hackrobat = addHackrobat(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hackrobat)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hackrobat)).isEqualTo(1);
    }

    @Test
    @DisplayName("Hackrobat's activated abilities wear off at end of turn")
    void abilitiesWearOffAtEndOfTurn() {
        Permanent hackrobat = addHackrobat(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, hackrobat, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.getEffectivePower(gd, hackrobat)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hackrobat)).isEqualTo(3);
    }

    private Permanent addHackrobat(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new Hackrobat());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
