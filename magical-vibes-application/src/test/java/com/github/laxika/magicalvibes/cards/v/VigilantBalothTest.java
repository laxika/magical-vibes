package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VigilantBaloth.class})
class VigilantBalothTest extends BaseCardTest {

    @Test
    @DisplayName("Vigilance keeps Vigilant Baloth untapped after attacking")
    void vigilanceDoesNotTapOnAttack() {
        Permanent baloth = addCreatureReady(player1, new VigilantBaloth());

        declareAttackers(List.of(0));

        assertThat(baloth.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Vigilance does not allow a tapped Vigilant Baloth to attack")
    void tappedBalothCannotAttack() {
        Permanent baloth = addCreatureReady(player1, new VigilantBaloth());
        baloth.tap();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(baloth.isTapped()).isTrue();
        assertThat(baloth.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Vigilance does not allow a summoning-sick Vigilant Baloth to attack")
    void summoningSickBalothCannotAttack() {
        Permanent baloth = harness.addToBattlefieldAndReturn(player1, new VigilantBaloth());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(baloth.isTapped()).isFalse();
        assertThat(baloth.isAttacking()).isFalse();
    }
}
