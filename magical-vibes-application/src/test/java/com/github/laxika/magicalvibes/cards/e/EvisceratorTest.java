package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.cards.m.MotherOfRunes;
import com.github.laxika.magicalvibes.cards.r.RadiantsJudgment;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Eviscerator.class, GiantCockroach.class, MotherOfRunes.class, RadiantsJudgment.class})
class EvisceratorTest extends BaseCardTest {

    @Test
    @DisplayName("Eviscerator has protection from white")
    void hasProtectionFromWhite() {
        Permanent eviscerator = harness.addToBattlefieldAndReturn(player1, new Eviscerator());

        assertThat(gqs.hasProtectionFrom(gd, eviscerator, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, eviscerator, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("A white spell cannot target Eviscerator")
    void cannotBeTargetedByWhiteSpell() {
        Permanent eviscerator = addCreatureReady(player2, new Eviscerator());
        addCreatureReady(player2, new GiantCockroach());

        harness.setHand(player1, List.of(new RadiantsJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, eviscerator.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }

    @Test
    @DisplayName("A white creature cannot block Eviscerator")
    void cannotBeBlockedByWhiteCreature() {
        Permanent eviscerator = addCreatureReady(player1, new Eviscerator());
        eviscerator.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new MotherOfRunes());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Eviscerator takes no combat damage from a white creature")
    void takesNoCombatDamageFromWhiteCreature() {
        Permanent attacker = addCreatureReady(player2, new MotherOfRunes());
        attacker.setAttacking(true);
        Permanent eviscerator = addCreatureReady(player1, new Eviscerator());
        eviscerator.setBlocking(true);
        eviscerator.addBlockingTarget(0);

        resolveCombat(player2);

        assertThat(eviscerator.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Eviscerator");
        harness.assertInGraveyard(player2, "Mother of Runes");
    }

    @Test
    @DisplayName("Entering the battlefield makes its controller lose 5 life")
    void enteringTheBattlefieldMakesControllerLoseFiveLife() {
        castEviscerator();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(15);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The ETB life-loss trigger is non-targeting")
    void etbLifeLossTriggerIsNonTargeting() {
        castEviscerator();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isNull();
    }

    private void castEviscerator() {
        harness.castFromHand(player1, new Eviscerator(), "{3}{B}{B}");
    }
}
