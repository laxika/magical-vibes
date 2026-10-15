package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WaryOkapi.class})
class WaryOkapiTest extends BaseCardTest {

    @Test
    @DisplayName("Vigilance keeps Wary Okapi untapped after attacking")
    void vigilanceDoesNotTapOnAttack() {
        Permanent okapi = addCreatureReady(player1, new WaryOkapi());

        declareAttackers(List.of(0));

        assertThat(okapi.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Vigilance does not allow a tapped Wary Okapi to attack")
    void tappedOkapiCannotAttack() {
        Permanent okapi = addCreatureReady(player1, new WaryOkapi());
        okapi.tap();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(okapi.isTapped()).isTrue();
        assertThat(okapi.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Vigilance does not bypass summoning sickness")
    void summoningSickOkapiCannotAttack() {
        Permanent okapi = harness.addToBattlefieldAndReturn(player1, new WaryOkapi());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(okapi.isTapped()).isFalse();
        assertThat(okapi.isAttacking()).isFalse();
    }
}
