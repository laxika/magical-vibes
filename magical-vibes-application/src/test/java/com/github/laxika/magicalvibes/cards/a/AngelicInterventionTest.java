package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BrittleEffigy;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AngelicIntervention.class, RuneclawBear.class, JaceBeleren.class, BrittleEffigy.class})
class AngelicInterventionTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on a target creature and grants chosen-color protection")
    void boostsCreatureAndGrantsProtection() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        castAt(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();

        harness.handleListChoice(player1, "RED");

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
    }

    @Test
    @DisplayName("Can grant protection from colorless")
    void grantsProtectionFromColorless() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        castAt(target);

        harness.handleListChoice(player1, "COLORLESS");

        assertThat(target.isProtectionFromColorlessUntilEndOfTurn()).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, target, null)).isTrue();
        assertThat(gqs.hasProtectionFromSource(gd, target, new Permanent(new BrittleEffigy()))).isTrue();
    }

    @Test
    @DisplayName("Grants protection to a target planeswalker without putting a +1/+1 counter on it")
    void protectsPlaneswalkerWithoutPlusOneCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        target.setCounterCount(CounterType.LOYALTY, 3);
        target.setSummoningSick(false);

        castAt(target);
        harness.handleListChoice(player1, "BLUE");

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLUE);
    }

    @Test
    @DisplayName("Cannot target a permanent that is not a creature or planeswalker you control")
    void cannotTargetOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new AngelicIntervention()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or planeswalker you control");
    }

    @Test
    void choosingWhiteStillPutsCounterOnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        castAt(target);
        harness.handleListChoice(player1, "WHITE");

        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.WHITE);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void cannotTargetOwnNoncreatureArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BrittleEffigy());
        harness.setHand(player1, List.of(new AngelicIntervention()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or planeswalker you control");
    }

    @Test
    void removedTargetDoesNotReceiveCounterOrPromptForProtection() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new AngelicIntervention()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void protectionExpiresButCounterRemains() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        castAt(target);
        harness.handleListChoice(player1, "COLORLESS");
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.isProtectionFromColorlessUntilEndOfTurn()).isFalse();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void castAt(Permanent target) {
        harness.setHand(player1, List.of(new AngelicIntervention()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
