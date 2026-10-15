package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(AlertShuInfantry.class)
class AlertShuInfantryTest extends BaseCardTest {

    @Test
    @DisplayName("Vigilance keeps Alert Shu Infantry untapped after attacking")
    void vigilanceKeepsItUntappedWhenItAttacks() {
        Permanent infantry = addCreatureReady(player1, new AlertShuInfantry());

        declareAttackers(List.of(0));

        assertThat(infantry.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Vigilance does not allow a tapped creature to attack")
    void tappedInfantryCannotAttack() {
        Permanent infantry = addCreatureReady(player1, new AlertShuInfantry());
        infantry.tap();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        assertThat(infantry.isAttacking()).isFalse();
        assertThat(infantry.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Vigilance does not bypass summoning sickness")
    void summoningSickInfantryCannotAttack() {
        Permanent infantry = addCreatureReady(player1, new AlertShuInfantry());
        infantry.setSummoningSick(true);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        assertThat(infantry.isAttacking()).isFalse();
        assertThat(infantry.isTapped()).isFalse();
    }
}
