package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArdentMilitia.class})
class ArdentMilitiaTest extends BaseCardTest {

    @Test
    void vigilanceKeepsItUntappedWhenItAttacks() {
        Permanent militia = addCreatureReady(player1, new ArdentMilitia());

        declareAttackers(List.of(0));

        assertThat(militia.isTapped()).isFalse();
    }

    @Test
    void vigilanceDoesNotAllowAttackingWhileTapped() {
        Permanent militia = addCreatureReady(player1, new ArdentMilitia());
        militia.tap();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        assertThat(militia.isTapped()).isTrue();
        assertThat(militia.isAttacking()).isFalse();
    }

    @Test
    void vigilanceDoesNotAllowAttackingWithSummoningSickness() {
        Permanent militia = addCreatureReady(player1, new ArdentMilitia());
        militia.setSummoningSick(true);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        assertThat(militia.isTapped()).isFalse();
        assertThat(militia.isAttacking()).isFalse();
    }
}
