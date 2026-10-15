package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CalciteSnapper;
import com.github.laxika.magicalvibes.cards.g.GoliathSphinx;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

@CardUsed({MordantDragon.class, GoliathSphinx.class, CalciteSnapper.class})
class MordantDragonTest extends BaseCardTest {

    @Test
    @DisplayName("{1}{R} ability gives +1/+0 until end of turn")
    void firebreathingBoostsPower() {
        Permanent dragon = addCreatureReady(player1, new MordantDragon());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dragon.getPowerModifier()).isEqualTo(1);
        assertThat(dragon.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Combat damage trigger targets before resolution and may deal the combat damage amount")
    void combatDamageTriggerDealsDamageEqualToCombatDamage() {
        Permanent dragon = addCreatureReady(player1, new MordantDragon());
        dragon.setAttacking(true);
        Permanent target = addCreatureReady(player2, new GoliathSphinx());
        addCreatureReady(player1, new GoliathSphinx());

        resolveCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        assertThat(target.getMarkedDamage()).isZero();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getMarkedDamage()).isEqualTo(5);
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Declining the combat damage trigger deals no additional damage")
    void decliningCombatDamageTriggerDealsNoAdditionalDamage() {
        Permanent dragon = addCreatureReady(player1, new MordantDragon());
        dragon.setAttacking(true);
        Permanent target = addCreatureReady(player2, new GoliathSphinx());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Repeated activations stack and their boosts expire at cleanup")
    void repeatedBoostsExpireAtCleanup() {
        Permanent dragon = addCreatureReady(player1, new MordantDragon());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dragon.getPowerModifier()).isEqualTo(2);
        assertThat(dragon.getToughnessModifier()).isZero();

        harness.passUntil(player1, TurnStep.CLEANUP);

        assertThat(dragon.getPowerModifier()).isZero();
        assertThat(dragon.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The boost cannot be activated without the red mana in its cost")
    void boostRequiresRedMana() {
        Permanent dragon = addCreatureReady(player1, new MordantDragon());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(dragon.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Triggered damage uses damage already dealt, even if the Dragon is boosted again")
    void triggeredDamageSnapshotsBoostedCombatDamage() {
        Permanent dragon = addCreatureReady(player1, new MordantDragon());
        Permanent target = addCreatureReady(player2, new GoliathSphinx());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        dragon.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.assertLife(player2, 14);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(dragon.getPowerModifier()).isEqualTo(2);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getMarkedDamage()).isEqualTo(6);
    }

    @Test
    @DisplayName("A creature with shroud is not an eligible damage target")
    void shroudCreatureCannotBeChosen() {
        Permanent dragon = addCreatureReady(player1, new MordantDragon());
        dragon.setAttacking(true);
        Permanent snapper = addCreatureReady(player2, new CalciteSnapper());
        Permanent target = addCreatureReady(player2, new GoliathSphinx());

        resolveCombat();
        resolveAllTriggers();

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MultiPermanentChoice choice) {
            assertThat(choice.validIds()).containsExactly(target.getId()).doesNotContain(snapper.getId());
            harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));
        } else {
            PendingInteraction.PermanentChoice choice =
                    gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
            assertThat(choice).isNotNull();
            assertThat(choice.validIds()).containsExactly(target.getId()).doesNotContain(snapper.getId());
            harness.handlePermanentChosen(player1, target.getId());
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(snapper.getMarkedDamage()).isZero();
        assertThat(target.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    @DisplayName("The damage trigger cannot remain on the stack without a legal creature target")
    void noCreatureTargetDoesNotOfferMayChoice() {
        Permanent dragon = addCreatureReady(player1, new MordantDragon());
        dragon.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Dealing combat damage only to a blocker does not trigger the player-damage ability")
    void blockedDragonDoesNotTrigger() {
        addCreatureReady(player1, new MordantDragon());
        addCreatureReady(player2, new GoliathSphinx());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Mordant Dragon");
    }
}
