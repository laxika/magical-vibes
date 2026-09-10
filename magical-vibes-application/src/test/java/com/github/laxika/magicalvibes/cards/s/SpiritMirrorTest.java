package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FieldOfSouls;
import com.github.laxika.magicalvibes.cards.m.MoggConscripts;
import com.github.laxika.magicalvibes.cards.w.WindsOfRath;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpiritMirror.class, FieldOfSouls.class, MoggConscripts.class, WindsOfRath.class})
class SpiritMirrorTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger creates a 2/2 Reflection token")
    void upkeepCreatesReflection() {
        addMirror(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Reflection");
        assertThat(token.getEffectivePower()).isEqualTo(2);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Trigger does not fire while a Reflection token is on the battlefield")
    void doesNotTriggerWithReflectionPresent() {
        addMirror(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Reflection")).isEqualTo(1);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Reflection")).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's Reflection token also stops the trigger")
    void opponentReflectionStopsTrigger() {
        addMirror(player1);
        addMirror(player2);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(countPermanents(player2, "Reflection")).isEqualTo(1);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Reflection")).isZero();
    }

    @Test
    @DisplayName("Two upkeep triggers create only one Reflection token")
    void simultaneousTriggersCreateOnlyOneReflection() {
        addMirror(player1);
        addMirror(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Reflection")).isEqualTo(1);
    }

    @Test
    @DisplayName("{0} ability destroys a target Reflection")
    void abilityDestroysReflection() {
        addMirror(player1);
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Reflection");

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, token.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Reflection")).isZero();
    }

    @Test
    @DisplayName("{0} ability can destroy an opponent's Reflection")
    void abilityDestroysOpponentsReflection() {
        addMirror(player1);
        addMirror(player2);

        advanceToUpkeep(player2);
        resolveAllTriggers();
        Permanent token = findPermanent(player2, "Reflection");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, token.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Reflection")).isZero();
    }

    @Test
    @DisplayName("Destroying the Reflection lets the next upkeep make a new one")
    void newTokenAfterDestruction() {
        addMirror(player1);
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Reflection");

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, token.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Reflection")).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a non-Reflection creature")
    void cannotTargetNonReflection() {
        addMirror(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MoggConscripts());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A non-Reflection token does not prevent the upkeep trigger")
    void nonReflectionTokenDoesNotStopTrigger() {
        addMirror(player1);
        harness.addToBattlefield(player1, new FieldOfSouls());
        harness.addToBattlefield(player1, new MoggConscripts());
        harness.setHand(player1, List.of(new WindsOfRath()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.forceActivePlayer(player1);

        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
        assertThat(countPermanents(player1, "Reflection")).isEqualTo(1);
    }

    private Permanent addMirror(Player player) {
        return harness.addToBattlefieldAndReturn(player, new SpiritMirror());
    }
}
