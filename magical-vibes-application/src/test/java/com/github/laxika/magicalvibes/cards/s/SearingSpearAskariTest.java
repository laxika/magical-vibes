package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FemerefScouts;
import com.github.laxika.magicalvibes.cards.m.MtendaHerder;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SearingSpearAskari.class, FemerefScouts.class, MtendaHerder.class})
class SearingSpearAskariTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability grants menace until end of turn")
    void resolvingAbilityGrantsMenace() {
        Permanent askari = addCreatureReady(player1, new SearingSpearAskari());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, askari, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Menace wears off at end of turn")
    void menaceWearsOff() {
        Permanent askari = addCreatureReady(player1, new SearingSpearAskari());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, askari, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Activating the ability does not tap Searing Spear Askari")
    void activatingDoesNotTap() {
        Permanent askari = addCreatureReady(player1, new SearingSpearAskari());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(askari.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ability can be activated while Searing Spear Askari has summoning sickness")
    void activatingWorksWhileSummoningSick() {
        Permanent askari = harness.addToBattlefieldAndReturn(player1, new SearingSpearAskari());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(askari.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Ability requires red mana")
    void requiresRedMana() {
        addCreatureReady(player1, new SearingSpearAskari());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Flanking gives a non-flanking blocker -1/-1 until end of turn")
    void flankingShrinksNonFlankingBlocker() {
        Permanent askari = addCreatureReady(player1, new SearingSpearAskari());
        askari.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new FemerefScouts());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isZero();
        assertThat(blocker.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(1);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Flanking does not affect a blocker that also has flanking")
    void flankingDoesNotShrinkFlankingBlocker() {
        Permanent askari = addCreatureReady(player1, new SearingSpearAskari());
        askari.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new MtendaHerder());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(blocker.getEffectivePower()).isEqualTo(1);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("A single blocker cannot block after the Askari gains menace")
    void gainedMenaceRequiresTwoBlockers() {
        Permanent askari = addCreatureReady(player1, new SearingSpearAskari());
        addCreatureReady(player2, new FemerefScouts());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        askari.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked except by two or more creatures");
    }
}
