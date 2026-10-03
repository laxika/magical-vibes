package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Brushstrider.class})
class BrushstriderTest extends BaseCardTest {

    @Test
    void attackingDoesNotTapBrushstrider() {
        Permanent brushstrider = addCreatureReady(player1, new Brushstrider());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(brushstrider.isAttacking()).isTrue();
        assertThat(brushstrider.isTapped()).isFalse();
    }

    @Test
    void vigilanceDoesNotAllowAttackingWhileTapped() {
        Permanent brushstrider = addCreatureReady(player1, new Brushstrider());
        brushstrider.tap();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(brushstrider.isAttacking()).isFalse();
        assertThat(brushstrider.isTapped()).isTrue();
    }

    @Test
    void vigilanceDoesNotAllowAttackingWithSummoningSickness() {
        Permanent brushstrider = addCreatureReady(player1, new Brushstrider());
        brushstrider.setSummoningSick(true);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(brushstrider.isAttacking()).isFalse();
        assertThat(brushstrider.isTapped()).isFalse();
    }
}
