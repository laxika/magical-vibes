package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.f.FireElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        Permanent forgeTender = addReadyForgeTender(player2);
        Permanent otherTarget = addReadyCreature(player2);

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
        addReadyForgeTender(player1);
        addReadyRedCreature(player2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Burrenton Forge-Tender"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Burrenton Forge-Tender"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving ability prompts for a red source choice")
    void resolvingAbilityPromptsForRedSourceChoice() {
        addReadyForgeTender(player1);
        addReadyRedCreature(player2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null).isTrue();
    }

    @Test
    @DisplayName("Chosen red source is prevented from dealing damage globally")
    void chosenRedSourcePreventedGlobally() {
        addReadyForgeTender(player1);
        Permanent redAttacker = addReadyRedCreature(player2);

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
        addReadyForgeTender(player1);
        Permanent redAttacker = addReadyRedCreature(player2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, redAttacker.getId());

        redAttacker.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Non-red creatures are not valid source choices")
    void nonRedSourceNotRecordedWhenOnlyGreenOnBattlefield() {
        addReadyForgeTender(player1);
        Permanent greenCreature = addReadyCreature(player2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.permanentsPreventedFromDealingDamage).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("No permanents on the battlefield"));
    }

    @Test
    @DisplayName("Prevention is cleared at end of turn")
    void preventionClearedAtEndOfTurn() {
        addReadyForgeTender(player1);
        Permanent redAttacker = addReadyRedCreature(player2);

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
        addReadyRedCreature(player2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addReadyForgeTender(Player player) {
        BurrentonForgeTender card = new BurrentonForgeTender();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyRedCreature(Player player) {
        FireElemental card = new FireElemental();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
