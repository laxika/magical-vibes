package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FireElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PilgrimOfJustice.class, FireElemental.class, GrizzlyBears.class, LightningBolt.class, Purelace.class})
class PilgrimOfJusticeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting resolves to the battlefield")
    void castAndResolve() {
        harness.castFromHand(player1, new PilgrimOfJustice(), "{2}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Pilgrim of Justice");
    }

    @Test
    @DisplayName("Cannot be targeted by a red instant")
    void cannotBeTargetedByRedInstant() {
        Permanent pilgrim = addCreatureReady(player2, new PilgrimOfJustice());
        Permanent otherTarget = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, pilgrim.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");

        harness.castInstant(player1, 0, otherTarget.getId());
    }

    @Test
    @DisplayName("Activating the ability sacrifices Pilgrim of Justice")
    void activatingAbilitySacrificesAndPutsOnStack() {
        addCreatureReady(player1, new PilgrimOfJustice());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Pilgrim of Justice");
        harness.assertInGraveyard(player1, "Pilgrim of Justice");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving the ability prompts for a red source")
    void resolvingAbilityPromptsForRedSourceChoice() {
        addCreatureReady(player1, new PilgrimOfJustice());
        addReadyRedCreature(player2);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("The chosen red source's next damage is prevented")
    void chosenRedSourceNextDamageIsPrevented() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new PilgrimOfJustice());
        Permanent redAttacker = addReadyRedCreature(player2);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, redAttacker.getId());

        redAttacker.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("Can choose a red spell on the stack and prevent its next damage to a creature")
    void preventsDamageFromRedSpellOnStackToCreature() {
        addCreatureReady(player1, new PilgrimOfJustice());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());
        LightningBolt lightningBolt = new LightningBolt();

        harness.setHand(player2, List.of(lightningBolt));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, victim.getId());
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(lightningBolt.getId());
        harness.handlePermanentChosen(player1, lightningBolt.getId());
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("Non-red sources cannot be chosen")
    void nonRedSourcesAreNotValidChoices() {
        addCreatureReady(player1, new PilgrimOfJustice());
        addCreatureReady(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
        assertThat(gameLogContains("No permanents on the battlefield")).isTrue();
    }

    @Test
    @DisplayName("The shield does not prevent damage if the chosen source is no longer red")
    void chosenSourceMustStillBeRedWhenItDealsDamage() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new PilgrimOfJustice());
        Permanent attacker = addReadyRedCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, attacker.getId());

        harness.ensurePriority(player1);
        harness.setHand(player1, List.of(new Purelace()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, attacker.getId());

        attacker.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 15);
        assertThat(gd.sourceNextDamageToAnyTargetShields).hasSize(1);
    }

    @Test
    @DisplayName("Protection prevents combat damage from a red attacker")
    void protectionPreventsRedCombatDamage() {
        Permanent pilgrim = addCreatureReady(player1, new PilgrimOfJustice());
        addReadyRedCreature(player2);
        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Pilgrim of Justice");
        assertThat(pilgrim.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A red creature cannot block Pilgrim of Justice")
    void protectionPreventsRedBlocker() {
        addCreatureReady(player1, new PilgrimOfJustice());
        addReadyRedCreature(player2);
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The shield prevents only the chosen source's damage")
    void otherRedSourceStillDealsDamage() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new PilgrimOfJustice());
        Permanent chosen = addReadyRedCreature(player2);
        Permanent other = addReadyRedCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        chosen.setAttacking(true);
        other.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 15);
    }

    private Permanent addReadyRedCreature(Player player) {
        return addCreatureReady(player, new FireElemental());
    }
}
