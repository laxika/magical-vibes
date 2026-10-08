package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VernadiShieldmate.class})
class VernadiShieldmateTest extends BaseCardTest {

    @Test
    @DisplayName("Vigilance keeps Vernadi Shieldmate untapped after attacking")
    void vigilanceDoesNotTapOnAttack() {
        Permanent shieldmate = addCreatureReady(player1, new VernadiShieldmate());

        declareAttackers(List.of(0));

        assertThat(shieldmate.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Vigilance does not allow a tapped Vernadi Shieldmate to attack")
    void tappedShieldmateCannotAttack() {
        Permanent shieldmate = addCreatureReady(player1, new VernadiShieldmate());
        shieldmate.tap();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(shieldmate.isTapped()).isTrue();
        assertThat(shieldmate.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Vigilance does not allow a summoning-sick Vernadi Shieldmate to attack")
    void summoningSickShieldmateCannotAttack() {
        Permanent shieldmate = harness.addToBattlefieldAndReturn(player1, new VernadiShieldmate());
        shieldmate.setSummoningSick(true);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(shieldmate.isAttacking()).isFalse();
    }
}
