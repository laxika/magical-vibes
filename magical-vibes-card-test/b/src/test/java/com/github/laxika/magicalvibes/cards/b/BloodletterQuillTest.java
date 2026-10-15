package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodletterQuill.class, Forest.class})
class BloodletterQuillTest extends BaseCardTest {

    @Test
    void drawsAndLosesLifeForEachBloodCounter() {
        Permanent quill = addQuill();
        quill.setCounterCount(CounterType.BLOOD, 2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(quill.getCounterCount(CounterType.BLOOD)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(quill.isTapped()).isTrue();
    }

    @Test
    void drawsAndLosesOneLifeWhenThereAreNoBloodCounters() {
        Permanent quill = addQuill();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(quill.getCounterCount(CounterType.BLOOD)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    void lifeLossUsesBloodCountersWhenTheAbilityResolves() {
        Permanent quill = addQuill();
        quill.setCounterCount(CounterType.BLOOD, 2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(quill.getCounterCount(CounterType.BLOOD)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void cannotActivateDrawAbilityWhileQuillIsTapped() {
        Permanent quill = addQuill();
        quill.setCounterCount(CounterType.BLOOD, 1);
        quill.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void removesBloodCounterForBlueAndBlackMana() {
        Permanent quill = addQuill();
        quill.setCounterCount(CounterType.BLOOD, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(quill.getCounterCount(CounterType.BLOOD)).isEqualTo(1);
        assertThat(quill.isTapped()).isFalse();
    }

    @Test
    void canActivateCounterRemovalWhenNoneArePresent() {
        Permanent quill = addQuill();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(quill.getCounterCount(CounterType.BLOOD)).isZero();
    }

    @Test
    void bloodCounterIsRemovedOnlyWhenAbilityResolves() {
        Permanent quill = addQuill();
        quill.setCounterCount(CounterType.BLOOD, 2);
        quill.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(quill.getCounterCount(CounterType.BLOOD)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(quill.getCounterCount(CounterType.BLOOD)).isEqualTo(1);
        assertThat(quill.isTapped()).isTrue();
    }

    @Test
    void multipleRemovalActivationsCanBeStackedWithOnlyOneCounter() {
        Permanent quill = addQuill();
        quill.setCounterCount(CounterType.BLOOD, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(quill.getCounterCount(CounterType.BLOOD)).isZero();
    }

    @Test
    void drawActivationAddsBloodCounterImmediatelyAndIgnoresOtherCounterTypes() {
        Permanent quill = addQuill();
        quill.setCounterCount(CounterType.CHARGE, 4);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(quill.getCounterCount(CounterType.BLOOD)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(quill.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
    }

    private Permanent addQuill() {
        return harness.addToBattlefieldAndReturn(player1, new BloodletterQuill());
    }
}
