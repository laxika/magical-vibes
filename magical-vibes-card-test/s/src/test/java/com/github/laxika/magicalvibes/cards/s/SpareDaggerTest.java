package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DireWolfProwler;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpareDagger.class, DireWolfProwler.class})
class SpareDaggerTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+0")
    void equippedCreatureGetsBoost() {
        Permanent bears = addCreatureReady(player1, new DireWolfProwler());
        attachDagger(player1, bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking with the equipped creature offers the sacrifice and deals 1 damage")
    void acceptingSacrificeDealsDamage() {
        Permanent bears = addCreatureReady(player1, new DireWolfProwler());
        attachDagger(player1, bears);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.MayAbilityTriggerTarget.class);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();
        resolveCombat();

        harness.assertInGraveyard(player1, "Spare Dagger");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Declining the sacrifice keeps Spare Dagger attached")
    void decliningKeepsDaggerAttached() {
        Permanent bears = addCreatureReady(player1, new DireWolfProwler());
        Permanent dagger = attachDagger(player1, bears);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);
        resolveCombat();

        harness.assertOnBattlefield(player1, "Spare Dagger");
        assertThat(dagger.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Only the equipped creature gets the attack trigger")
    void otherCreatureAttackingDoesNotTrigger() {
        Permanent equipped = addCreatureReady(player1, new DireWolfProwler());
        addCreatureReady(player1, new DireWolfProwler());
        attachDagger(player1, equipped);

        declareAttackers(player1, List.of(1));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Spare Dagger");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void equipCostsOneManaAndMovesTheBonus() {
        Permanent first = addCreatureReady(player1, new DireWolfProwler());
        Permanent second = addCreatureReady(player1, new DireWolfProwler());
        Permanent dagger = attachDagger(player1, first);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 2, null, second.getId());
        assertThat(dagger.getAttachedTo()).isEqualTo(first.getId());
        harness.passBothPriorities();

        assertThat(dagger.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
    }

    @Test
    void equipRejectsOpposingCreaturesAndNoncreatures() {
        Permanent dagger = harness.addToBattlefieldAndReturn(player1, new SpareDagger());
        Permanent opponent = addCreatureReady(player2, new DireWolfProwler());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, dagger.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(dagger.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipCannotBeActivatedDuringCombat() {
        Permanent dagger = harness.addToBattlefieldAndReturn(player1, new SpareDagger());
        Permanent creature = addCreatureReady(player1, new DireWolfProwler());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(dagger.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void damageCanTargetItsController() {
        Permanent creature = addCreatureReady(player1, new DireWolfProwler());
        attachDagger(player1, creature);
        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Spare Dagger");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void creatureDealsTheDamageToATargetCreature() {
        Permanent creature = addCreatureReady(player1, new DireWolfProwler());
        Permanent target = addCreatureReady(player2, new DireWolfProwler());
        attachDagger(player1, creature);
        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertInGraveyard(player1, "Spare Dagger");
        assertThat(target.getMarkedDamage()).isZero();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(target.getMarkedDamageBySource()).containsEntry(creature.getId(), 1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    void removingDaggerBeforeResolutionPreventsDamage() {
        Permanent creature = addCreatureReady(player1, new DireWolfProwler());
        Permanent dagger = attachDagger(player1, creature);
        declareAttackers(player1, List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(dagger);
        gd.playerGraveyards.get(player1.getId()).add(dagger.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void attackTriggerStillDealsDamageAfterTheAttackerLeaves() {
        Permanent creature = addCreatureReady(player1, new DireWolfProwler());
        Permanent dagger = attachDagger(player1, creature);
        declareAttackers(player1, List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());
        dagger.setAttachedTo(null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();
        resolveCombat();

        harness.assertInGraveyard(player1, "Spare Dagger");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    private Permanent attachDagger(Player player, Permanent host) {
        Permanent dagger = harness.addToBattlefieldAndReturn(player, new SpareDagger());
        dagger.setAttachedTo(host.getId());
        return dagger;
    }
}
