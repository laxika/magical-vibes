package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SaruliCaretaker.class})
class SaruliCaretakerTest extends BaseCardTest {

    @Test
    @DisplayName("Taps itself and another creature to add mana of the chosen color")
    void tapsItselfAndAnotherCreatureForMana() {
        Permanent caretaker = addCreatureReady(player1, new SaruliCaretaker());
        Permanent creature = addCreatureReady(player1, new SaruliCaretaker());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(caretaker.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate without another untapped creature you control")
    void requiresAnotherUntappedCreature() {
        Permanent caretaker = addCreatureReady(player1, new SaruliCaretaker());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(caretaker.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot tap a creature controlled by an opponent")
    void requiresCreatureYouControl() {
        Permanent caretaker = addCreatureReady(player1, new SaruliCaretaker());
        addCreatureReady(player2, new SaruliCaretaker());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(caretaker.isTapped()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Produces exactly one mana of any chosen color without using the stack")
    void producesAnyColorAsManaAbility(ManaColor color) {
        Permanent caretaker = addCreatureReady(player1, new SaruliCaretaker());
        Permanent creature = addCreatureReady(player1, new SaruliCaretaker());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(caretaker.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can tap a summoning-sick creature to pay the additional cost")
    void canTapSummoningSickSupportCreature() {
        Permanent caretaker = addCreatureReady(player1, new SaruliCaretaker());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SaruliCaretaker());
        creature.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(caretaker.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("A summoning-sick Caretaker cannot activate its tap ability")
    void summoningSickCaretakerCannotActivate() {
        Permanent caretaker = harness.addToBattlefieldAndReturn(player1, new SaruliCaretaker());
        caretaker.setSummoningSick(true);
        Permanent creature = addCreatureReady(player1, new SaruliCaretaker());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(caretaker.isTapped()).isFalse();
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An already tapped creature cannot pay the additional cost")
    void tappedSupportCreatureCannotPayCost() {
        Permanent caretaker = addCreatureReady(player1, new SaruliCaretaker());
        Permanent creature = addCreatureReady(player1, new SaruliCaretaker());
        creature.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(caretaker.isTapped()).isFalse();
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An already tapped Caretaker cannot activate")
    void tappedCaretakerCannotActivate() {
        Permanent caretaker = addCreatureReady(player1, new SaruliCaretaker());
        caretaker.tap();
        Permanent creature = addCreatureReady(player1, new SaruliCaretaker());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Taps only the chosen creature when several can pay the cost")
    void choosesOneSupportCreature() {
        Permanent caretaker = addCreatureReady(player1, new SaruliCaretaker());
        Permanent unchosen = addCreatureReady(player1, new SaruliCaretaker());
        Permanent chosen = addCreatureReady(player1, new SaruliCaretaker());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, chosen.getId());
        harness.handleListChoice(player1, "RED");

        assertThat(caretaker.isTapped()).isTrue();
        assertThat(chosen.isTapped()).isTrue();
        assertThat(unchosen.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
