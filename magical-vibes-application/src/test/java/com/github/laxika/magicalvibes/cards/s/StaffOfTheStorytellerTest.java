package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CallTheCavalry;
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

@CardUsed({StaffOfTheStoryteller.class, Forest.class, CallTheCavalry.class})
class StaffOfTheStorytellerTest extends BaseCardTest {

    @Test
    void enteringCreatesSpiritAndAddsStoryCounterWhenItEnters() {
        harness.setHand(player1, List.of(new StaffOfTheStoryteller()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent staff = findPermanent(player1, "Staff of the Storyteller");
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        assertThat(staff.getCounterCount(CounterType.STORY)).isEqualTo(1);
    }

    @Test
    void creatingMultipleTokensAddsOneStoryCounterForTheBatch() {
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new StaffOfTheStoryteller());
        harness.setHand(player1, List.of(new CallTheCavalry()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(staff.getCounterCount(CounterType.STORY)).isEqualTo(1);
    }

    @Test
    void removingStoryCounterAndPayingWhiteManaDrawsCard() {
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new StaffOfTheStoryteller());
        staff.setCounterCount(CounterType.STORY, 1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(staff.getCounterCount(CounterType.STORY)).isZero();
        assertThat(staff.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void cannotDrawWithoutAStoryCounter() {
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new StaffOfTheStoryteller());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
