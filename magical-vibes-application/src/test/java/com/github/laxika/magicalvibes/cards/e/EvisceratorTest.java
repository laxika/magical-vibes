package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EvisceratorTest extends BaseCardTest {

    @Test
    @DisplayName("Eviscerator has protection from white")
    void hasProtectionFromWhite() {
        Permanent eviscerator = harness.addToBattlefieldAndReturn(player1, new Eviscerator());

        assertThat(gqs.hasProtectionFrom(gd, eviscerator, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, eviscerator, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("Entering the battlefield makes its controller lose 5 life")
    void enteringTheBattlefieldMakesControllerLoseFiveLife() {
        castEviscerator();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(15);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The ETB life-loss trigger is non-targeting")
    void etbLifeLossTriggerIsNonTargeting() {
        castEviscerator();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isNull();
    }

    private void castEviscerator() {
        harness.setHand(player1, List.of(new Eviscerator()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castCreature(player1, 0);
    }
}
