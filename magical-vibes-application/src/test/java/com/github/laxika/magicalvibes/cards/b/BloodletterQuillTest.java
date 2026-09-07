package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodletterQuill.class, GrizzlyBears.class})
class BloodletterQuillTest extends BaseCardTest {

    @Test
    void drawsAndLosesLifeForEachBloodCounter() {
        Permanent quill = addQuill();
        quill.setCounterCount(CounterType.BLOOD, 2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(quill.getCounterCount(CounterType.BLOOD)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(quill.isTapped()).isTrue();
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
    }

    @Test
    void cannotRemoveBloodCounterWhenNoneArePresent() {
        addQuill();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addQuill() {
        return harness.addToBattlefieldAndReturn(player1, new BloodletterQuill());
    }
}
