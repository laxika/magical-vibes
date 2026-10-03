package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(AffaProtector.class)
class AffaProtectorTest extends BaseCardTest {

    @Test
    @DisplayName("Vigilance: Affa Protector does not tap when declared as attacker")
    void vigilancePreventsTapWhenAttacking() {
        Permanent affaProtector = addCreatureReady(player1, new AffaProtector());

        declareAttackers(List.of(0));

        assertThat(affaProtector.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Vigilance does not allow a tapped Affa Protector to attack")
    void tappedCreatureCannotAttack() {
        Permanent affaProtector = addCreatureReady(player1, new AffaProtector());
        affaProtector.tap();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        assertThat(affaProtector.isAttacking()).isFalse();
        assertThat(affaProtector.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Vigilance does not allow a summoning-sick Affa Protector to attack")
    void summoningSickCreatureCannotAttack() {
        Permanent affaProtector = harness.addToBattlefieldAndReturn(player1, new AffaProtector());
        affaProtector.setSummoningSick(true);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        assertThat(affaProtector.isAttacking()).isFalse();
        assertThat(affaProtector.isTapped()).isFalse();
    }
}
