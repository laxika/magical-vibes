package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.m.MakindiOx;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({AttendedHealer.class, MakindiOx.class})
class AttendedHealerTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Cat the first time its controller gains life each turn")
    void createsCatOnFirstLifeGainEachTurn() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, new AttendedHealer());
        harness.setLife(player1, 20);

        gainLife(1);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Cat")).isEqualTo(1);

        gainLife(1);
        assertThat(gd.stack).isEmpty();

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.passUntil(player1, TurnStep.UPKEEP);
        gainLife(1);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Cat")).isEqualTo(2);
    }

    @Test
    @DisplayName("The activated ability grants lifelink to another Cleric until end of turn")
    void grantsLifelinkToAnotherClericUntilEndOfTurn() {
        harness.addToBattlefield(player1, new AttendedHealer());
        Permanent cleric = harness.addToBattlefieldAndReturn(player1, new AttendedHealer());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, cleric.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cleric, Keyword.LIFELINK)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, cleric, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("The activated ability cannot target itself or a non-Cleric")
    void activatedAbilityRequiresAnotherCleric() {
        Permanent healer = harness.addToBattlefieldAndReturn(player1, new AttendedHealer());
        Permanent ox = harness.addToBattlefieldAndReturn(player1, new MakindiOx());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, healer.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, ox.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not create a Cat if life was gained before the Healer entered this turn")
    void doesNotTriggerAfterEarlierLifeGainBeforeEntering() {
        harness.setHand(player2, List.of());
        gainLife(1);

        harness.castFromHand(player1, new AttendedHealer(), "{3}{W}");
        harness.passBothPriorities();
        gainLife(1);

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Cat")).isZero();
    }

    @Test
    @DisplayName("A second Healer entering after the first life gain cannot trigger that turn")
    void newlyEnteredHealerDoesNotTriggerOnSecondLifeGain() {
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, new AttendedHealer());
        gainLife(1);
        harness.passBothPriorities();

        harness.castFromHand(player1, new AttendedHealer(), "{3}{W}");
        harness.passBothPriorities();
        gainLife(1);

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Cat")).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Healer triggers once for a single life-gaining event regardless of amount")
    void eachHealerCreatesOneCatForLargeLifeGain() {
        harness.addToBattlefield(player1, new AttendedHealer());
        harness.addToBattlefield(player1, new AttendedHealer());

        gainLife(10);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Cat")).isEqualTo(2);
        gainLife(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's life gain does not trigger or consume the controller's first gain")
    void opponentLifeGainDoesNotTrigger() {
        harness.addToBattlefield(player1, new AttendedHealer());
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3));
        assertThat(gd.stack).isEmpty();

        gainLife(1);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Cat")).isEqualTo(1);
    }

    @Test
    @DisplayName("Gaining zero life does not trigger or consume the first life gain")
    void zeroLifeGainDoesNotTrigger() {
        harness.addToBattlefield(player1, new AttendedHealer());
        gainLife(0);
        assertThat(gd.stack).isEmpty();

        gainLife(1);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Cat")).isEqualTo(1);
    }

    @Test
    @DisplayName("Can grant lifelink to an opponent's Cleric without tapping the Healer")
    void grantsLifelinkToOpponentsCleric() {
        Permanent healer = harness.addToBattlefieldAndReturn(player1, new AttendedHealer());
        Permanent cleric = harness.addToBattlefieldAndReturn(player2, new AttendedHealer());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, cleric.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cleric, Keyword.LIFELINK)).isTrue();
        assertThat(healer.isTapped()).isFalse();
    }

    private void gainLife(int amount) {
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), amount));
    }

}
