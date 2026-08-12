package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CircleOfProtectionBlueTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability prompts for a blue source choice")
    void resolvingAbilityPromptsForBlueSource() {
        addReadyCircle(player1);
        addReadyBlueCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Choosing a blue source records a one-shot prevention shield")
    void choosingBlueSourceRecordsShield() {
        addReadyCircle(player1);
        Permanent wizard = addReadyBlueCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, wizard.getId());

        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.playerId().equals(player1.getId()) && s.sourceId().equals(wizard.getId()));
    }

    @Test
    @DisplayName("Prevents the next combat damage from the chosen source and consumes the shield")
    void preventsNextCombatDamageAndConsumesShield() {
        harness.setLife(player1, 20);
        addReadyCircle(player1);
        Permanent wizard = addReadyBlueCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, wizard.getId());

        wizard.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Only the chosen source is prevented; a different blue source still deals damage")
    void differentSourceStillDealsDamage() {
        harness.setLife(player1, 20);
        addReadyCircle(player1);
        Permanent chosen = addReadyBlueCreature(player2);
        Permanent other = addReadyBlueCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        other.setAttacking(true);
        resolveCombat(player2);

        // The unchosen 1/1 deals its damage; the shield is untouched
        harness.assertLife(player1, 19);
        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.sourceId().equals(chosen.getId()));
    }

    @Test
    @DisplayName("Non-blue permanents are not valid source choices")
    void nonBlueSourceNotValid() {
        addReadyCircle(player1);
        addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("No permanents on the battlefield"));
    }

    @Test
    @DisplayName("A blue spell on the stack is a legal source choice")
    void blueSpellOnStackIsLegalSourceChoice() {
        addReadyCircle(player1);
        Card blueDamageSpell = new Card();
        blueDamageSpell.setName("Blue damage spell");
        blueDamageSpell.setType(CardType.INSTANT);
        blueDamageSpell.setManaCost("{U}");
        blueDamageSpell.setColor(CardColor.BLUE);
        blueDamageSpell.setColors(List.of(CardColor.BLUE));
        blueDamageSpell.target(new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY), "Target must be a player"))
                .addEffect(EffectSlot.SPELL, new DealDamageToPlayersEffect(1, DamageRecipient.TARGET_PLAYER));
        harness.setHand(player2, List.of(blueDamageSpell));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("Shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        addReadyCircle(player1);
        Permanent wizard = addReadyBlueCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, wizard.getId());

        assertThat(gd.playerSourceNextDamageShields).isNotEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    private Permanent addReadyCircle(Player player) {
        Permanent perm = new Permanent(new CircleOfProtectionBlue());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyBlueCreature(Player player) {
        Permanent perm = new Permanent(new FugitiveWizard());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
