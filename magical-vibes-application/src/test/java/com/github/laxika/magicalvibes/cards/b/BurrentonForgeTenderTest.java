package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FireElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BurrentonForgeTender.class, FireElemental.class, GrizzlyBears.class, LightningBolt.class})
class BurrentonForgeTenderTest extends BaseCardTest {

    @Test
    @DisplayName("Casting resolves to the battlefield")
    void castAndResolve() {
        harness.setHand(player1, List.of(new BurrentonForgeTender()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Burrenton Forge-Tender");
    }

    @Test
    @DisplayName("Cannot be targeted by red instant")
    void cannotBeTargetedByRedInstant() {
        Permanent forgeTender = addCreatureReady(player2, new BurrentonForgeTender());
        Permanent otherTarget = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, forgeTender.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");

        harness.castInstant(player1, 0, otherTarget.getId());
    }

    @Test
    @DisplayName("Activating ability sacrifices Forge-Tender and puts ability on the stack")
    void activatingAbilitySacrificesAndPutsOnStack() {
        addCreatureReady(player1, new BurrentonForgeTender());
        addCreatureReady(player2, new FireElemental());

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Burrenton Forge-Tender");
        harness.assertInGraveyard(player1, "Burrenton Forge-Tender");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving ability prompts for a red source choice")
    void resolvingAbilityPromptsForRedSourceChoice() {
        addCreatureReady(player1, new BurrentonForgeTender());
        addCreatureReady(player2, new FireElemental());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null).isTrue();
    }

    @Test
    @DisplayName("Chosen red source is prevented from dealing damage globally")
    void chosenRedSourcePreventedGlobally() {
        addCreatureReady(player1, new BurrentonForgeTender());
        Permanent redAttacker = addCreatureReady(player2, new FireElemental());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, redAttacker.getId());

        assertThat(gd.permanentsPreventedFromDealingDamage).contains(redAttacker.getId());
        assertThat(gd.playerSourceDamagePreventionIds.getOrDefault(player1.getId(), java.util.Set.of()))
                .doesNotContain(redAttacker.getId());
    }

    @Test
    @DisplayName("Prevented red creature deals no combat damage to any player")
    void preventsCombatDamageToAnyPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new BurrentonForgeTender());
        Permanent redAttacker = addCreatureReady(player2, new FireElemental());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, redAttacker.getId());

        redAttacker.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Prevents damage from a chosen red spell")
    void preventsDamageFromChosenRedSpell() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new BurrentonForgeTender());

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        var boltId = gd.stack.getLast().getTargetableId();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(boltId);
        harness.handlePermanentChosen(player1, boltId);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Non-red creatures are not valid source choices")
    void nonRedSourceNotRecordedWhenOnlyGreenOnBattlefield() {
        addCreatureReady(player1, new BurrentonForgeTender());
        addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.permanentsPreventedFromDealingDamage).isEmpty();
        assertThat(gameLogContains("No permanents on the battlefield")).isTrue();
    }

    @Test
    @DisplayName("Prevention is cleared at end of turn")
    void preventionClearedAtEndOfTurn() {
        addCreatureReady(player1, new BurrentonForgeTender());
        Permanent redAttacker = addCreatureReady(player2, new FireElemental());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, redAttacker.getId());

        assertThat(gd.permanentsPreventedFromDealingDamage).contains(redAttacker.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.permanentsPreventedFromDealingDamage).isEmpty();
    }

    @Test
    @DisplayName("Can activate with summoning sickness")
    void canActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new BurrentonForgeTender());
        addCreatureReady(player2, new FireElemental());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

}
