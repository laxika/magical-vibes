package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FrontlineRebel.class})
class FrontlineRebelTest extends BaseCardTest {

    @Test
    @DisplayName("Frontline Rebel must attack each combat when able")
    void mustAttackWhenAble() {
        addCreatureReady(player1, new FrontlineRebel());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Frontline Rebel can satisfy its requirement by attacking")
    void canAttack() {
        Permanent rebel = addCreatureReady(player1, new FrontlineRebel());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));

        assertThat(rebel.isAttacking()).isTrue();
        assertThat(rebel.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A tapped Frontline Rebel is not required to attack")
    void tappedRebelDoesNotHaveToAttack() {
        Permanent rebel = addCreatureReady(player1, new FrontlineRebel());
        rebel.setTapped(true);

        assertThatCode(() -> declareAttackers(List.of())).doesNotThrowAnyException();
        assertThat(rebel.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("A summoning-sick Frontline Rebel is not required to attack")
    void summoningSickRebelDoesNotHaveToAttack() {
        Permanent rebel = harness.addToBattlefieldAndReturn(player1, new FrontlineRebel());
        rebel.setSummoningSick(true);

        assertThatCode(() -> declareAttackers(List.of())).doesNotThrowAnyException();
        assertThat(rebel.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("An untapped Frontline Rebel must attack again in another combat")
    void mustAttackAgainInAnotherCombat() {
        Permanent rebel = addCreatureReady(player1, new FrontlineRebel());
        declareAttackers(List.of(0));
        rebel.setTapped(false);
        rebel.setAttacking(false);

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }
}
