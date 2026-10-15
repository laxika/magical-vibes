package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({FalconsWingHarness.class, GrizzlyBears.class, Shock.class, ProdigalPyromancer.class})
class FalconsWingHarnessTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Falcon's Wing Harness attaches it to a targeted creature and grants its abilities")
    void enteringAttachesAndGrantsAbilities() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FalconsWingHarness()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castArtifact(player1, 0, bears.getId());
        harness.passBothPriorities();

        Permanent harnessPermanent = findPermanent(player1, "Falcon's Wing Harness");
        assertThat(harnessPermanent.getAttachedTo()).isNull();

        harness.passBothPriorities();

        assertThat(harnessPermanent.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Equip attaches Falcon's Wing Harness to another creature you control")
    void equipAttachesToAnotherCreature() {
        Permanent harnessPermanent = addHarnessReady();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(harnessPermanent.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ward {1} counters an opponent's spell when its controller does not pay")
    void wardCountersUnpaidSpell() {
        Permanent harnessPermanent = addHarnessReady();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harnessPermanent.setAttachedTo(bears.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Ward lets an opponent's spell resolve when its controller pays {1}")
    void wardCanBePaid() {
        Permanent harnessPermanent = addHarnessReady();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harnessPermanent.setAttachedTo(bears.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void reequippingRemovesBonusesAndWardFromPreviousCreature() {
        Permanent equipment = addHarnessReady();
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        equipment.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isTrue();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, first.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void wardDoesNotTriggerForControllersSpell() {
        Permanent equipment = addHarnessReady();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        equipment.setAttachedTo(bears.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(bears.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void wardCountersSpellWhenOpponentCannotPay() {
        Permanent equipment = addHarnessReady();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        equipment.setAttachedTo(bears.getId());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bears.getId());
        resolveAllTriggers();

        assertThat(bears.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void wardCountersOpponentsActivatedAbility() {
        Permanent equipment = addHarnessReady();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        equipment.setAttachedTo(bears.getId());
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());

        harness.activateAbility(player2, 0, null, bears.getId());
        resolveAllTriggers();

        assertThat(pyromancer.isTapped()).isTrue();
        assertThat(bears.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipmentCanEnterWithoutAnyCreatureToAttachTo() {
        harness.castFromHand(player1, new FalconsWingHarness(), "{1}{U}");
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Falcon's Wing Harness").getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void enterTriggerDoesNotAttachWhenTargetLeavesBattlefield() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FalconsWingHarness()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castArtifact(player1, 0, bears.getId());
        harness.passBothPriorities();
        Permanent equipment = findPermanent(player1, "Falcon's Wing Harness");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, bears.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(equipment.getAttachedTo()).isNull();
        harness.assertOnBattlefield(player1, "Falcon's Wing Harness");
    }

    @Test
    void payingWardAllowsActivatedAbilityToResolve() {
        Permanent equipment = addHarnessReady();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        equipment.setAttachedTo(bears.getId());
        addCreatureReady(player2, new ProdigalPyromancer());
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player2, 0, null, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipCannotTargetOpponentsCreature() {
        addHarnessReady();
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void equipCannotBeActivatedDuringOpponentsTurn() {
        addHarnessReady();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private Permanent addHarnessReady() {
        return harness.addToBattlefieldAndReturn(player1, new FalconsWingHarness());
    }
}
