package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.f.Fog;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.s.SamiteHealer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({QuestingCosplayer.class, GrizzlyBears.class, Fog.class, GarrukWildspeaker.class, SamiteHealer.class})
class QuestingCosplayerTest extends BaseCardTest {

    @Test
    @DisplayName("creates a Questing Role attached to any target creature and grants Questing Beast's abilities")
    void createsQuestingRoleAndGrantsAbilities() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        attachQuestingRole(target);

        Permanent role = findPermanent(player1, "Questing Role");
        assertThat(role.getCard().isToken()).isTrue();
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("the enchanted creature cannot be blocked by creatures with power 2 or less")
    void cannotBeBlockedByLowPowerCreature() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        attachQuestingRole(target);
        target.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(target);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("combat damage is unpreventable but creatures are not legal targets for the granted trigger")
    void combatDamageCannotBePreventedAndDoesNotDamageDefendingCreature() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent defendingCreature = addCreatureReady(player2, new GrizzlyBears());
        attachQuestingRole(target);

        harness.setHand(player2, List.of(new Fog()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0);

        target.setAttacking(true);
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(defendingCreature);
    }

    @Test
    @DisplayName("combat damage from other controlled creatures cannot be prevented")
    void otherControlledCreaturesDealCombatDamageThroughFog() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        attachQuestingRole(target);
        harness.setHand(player2, List.of(new Fog()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0);

        target.setAttacking(true);
        otherAttacker.setAttacking(true);
        resolveCombat();

        harness.assertLife(player2, 16);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("the granted combat-damage trigger damages a defending planeswalker")
    void combatDamageTriggerDamagesDefendingPlaneswalker() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent planeswalker = harness.enterBattlefieldAndReturn(player2, new GarrukWildspeaker());
        attachQuestingRole(target);
        target.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 18);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("a second Role controlled by the same player replaces the first")
    void newestRoleReplacesPreviousRole() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        attachQuestingRole(target);
        Permanent firstRole = findPermanent(player1, "Questing Role");

        attachQuestingRole(target);

        assertThat(findPermanents(player1, "Questing Role")).hasSize(1).doesNotContain(firstRole);
        assertThat(findPermanent(player1, "Questing Role").getAttachedTo()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("the granted trigger deals noncombat damage that can be prevented")
    void noncombatDamageToPlaneswalkerCanBePrevented() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent planeswalker = harness.enterBattlefieldAndReturn(player2, new GarrukWildspeaker());
        Permanent healer = addCreatureReady(player2, new SamiteHealer());
        attachQuestingRole(target);
        int healerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(healer);
        harness.activateAbility(player2, healerIndex, null, planeswalker.getId());
        harness.passBothPriorities();
        target.setAttacking(true);

        resolveCombat();

        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("the granted haste and vigilance allow a summoning-sick creature to attack without tapping")
    void newlyEnteredEnchantedCreatureCanAttackWithoutTapping() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setSummoningSick(true);
        attachQuestingRole(target);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(target);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(attackerIndex)));

        assertThat(target.isAttacking()).isTrue();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("combat-damage protection follows the enchanted creature's controller")
    void opponentsEnchantedCreatureProtectsItsControllersOtherAttackers() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent otherAttacker = addCreatureReady(player2, new GrizzlyBears());
        attachQuestingRole(target);
        harness.setHand(player1, List.of(new Fog()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0);
        target.setAttacking(true);
        otherAttacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertLife(player1, 16);
    }

    @Test
    @DisplayName("Roles controlled by different players can coexist on the same creature")
    void rolesWithDifferentControllersRemainAttached() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        attachQuestingRole(target);
        Permanent firstRole = findPermanent(player1, "Questing Role");
        harness.enterBattlefieldAndReturn(player2, new QuestingCosplayer());
        harness.handlePermanentChosen(player2, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Questing Role")).containsExactly(firstRole);
        assertThat(findPermanents(player2, "Questing Role")).hasSize(1);
        assertThat(findPermanent(player2, "Questing Role").getAttachedTo()).isEqualTo(target.getId());
    }

    private void attachQuestingRole(Permanent target) {
        harness.setHand(player1, List.of(new QuestingCosplayer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
