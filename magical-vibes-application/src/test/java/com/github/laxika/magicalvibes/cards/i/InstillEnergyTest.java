package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InstillEnergy.class, GrizzlyBears.class})
class InstillEnergyTest extends BaseCardTest {

    // ===== Can attack as though it had haste =====

    @Test
    @DisplayName("Summoning-sick creature enchanted with Instill Energy can attack")
    void enchantedSummoningSickCreatureCanAttack() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bearsPerm.setSummoningSick(true);
        attachAura(bearsPerm);

        // A blocker on the defending side so combat pauses at declare-blockers (isAttacking stays set).
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(bearsPerm.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Summoning-sick creature without Instill Energy cannot attack")
    void summoningSickCreatureCannotAttackWithoutAura() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bearsPerm.setSummoningSick(true);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    // ===== {0}: Untap enchanted creature =====

    @Test
    @DisplayName("Activated ability untaps the enchanted creature")
    void activatedAbilityUntapsEnchantedCreature() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());
        bearsPerm.tap();
        attachAura(bearsPerm);

        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(bearsPerm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untap ability can only be activated once each turn")
    void untapAbilityOnlyOncePerTurn() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());
        bearsPerm.tap();
        attachAura(bearsPerm);

        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        bearsPerm.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Untap ability can be activated again on a new turn")
    void untapAbilityResetsEachTurn() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());
        attachAura(bearsPerm);

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        bearsPerm.tap();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(bearsPerm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untap ability can only be activated during your turn")
    void untapAbilityOnlyDuringYourTurn() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());
        bearsPerm.tap();
        attachAura(bearsPerm);

        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    private Permanent attachAura(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new InstillEnergy());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
