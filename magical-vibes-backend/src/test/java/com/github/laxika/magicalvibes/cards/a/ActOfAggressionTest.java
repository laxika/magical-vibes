package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetPermanentUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.UntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ActOfAggressionTest extends BaseCardTest {

    @Test
    @DisplayName("Act of Aggression has correct card properties")
    void hasCorrectProperties() {
        ActOfAggression card = new ActOfAggression();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(3);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(GainControlOfTargetPermanentUntilEndOfTurnEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(UntapTargetPermanentEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(2)).isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect effect = (GrantKeywordEffect) card.getEffects(EffectSlot.SPELL).get(2);
        assertThat(effect.keyword()).isEqualTo(Keyword.HASTE);
        assertThat(effect.scope()).isEqualTo(GrantScope.TARGET);
    }

    @Test
    @DisplayName("Casting Act of Aggression puts it on the stack with the target creature")
    void castingPutsOnStack() {
        Permanent target = addReadyCreature(player2);
        harness.setHand(player1, List.of(new ActOfAggression()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Act of Aggression");
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Resolving Act of Aggression untaps target, gains control, and grants haste")
    void resolvesUntapGainControlAndHaste() {
        Permanent target = addReadyCreature(player2);
        target.tap();
        harness.setHand(player1, List.of(new ActOfAggression()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.untilEndOfTurnStolenCreatures).contains(target.getId());
    }

    @Test
    @DisplayName("Stolen creature can attack this turn because Act of Aggression grants haste")
    void stolenCreatureCanAttackDueToHaste() {
        Permanent target = addReadyCreature(player2);
        target.setSummoningSick(false);
        harness.setHand(player1, List.of(new ActOfAggression()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        GameService gs = harness.getGameService();
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(target);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(attackerIndex));

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Control and haste expire at cleanup")
    void controlAndHasteExpireAtCleanup() {
        Permanent target = addReadyCreature(player2);
        harness.setHand(player1, List.of(new ActOfAggression()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.untilEndOfTurnStolenCreatures).doesNotContain(target.getId());
    }

    @Test
    @DisplayName("Cannot target own creature")
    void cannotTargetOwnCreature() {
        addReadyCreature(player2); // valid target so spell is playable
        Permanent ownCreature = addReadyCreature(player1);
        harness.setHand(player1, List.of(new ActOfAggression()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature an opponent controls");
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addReadyCreature(player2); // valid target so spell is playable
        Permanent enchantment = new Permanent(new Pacifism());
        gd.playerBattlefields.get(player2.getId()).add(enchantment);
        harness.setHand(player1, List.of(new ActOfAggression()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature an opponent controls");
    }

    @Test
    @DisplayName("Can be cast with only generic mana by paying 4 life for Phyrexian mana")
    void canPayPhyrexianManaWithLife() {
        Permanent target = addReadyCreature(player2);
        harness.setHand(player1, List.of(new ActOfAggression()));
        // Only 3 colorless mana — no red. Phyrexian {R/P}{R/P} must be paid with 4 life.
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        // Card resolved successfully
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        // Player paid 4 life (2 per Phyrexian symbol)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Pays Phyrexian mana with red mana when available instead of life")
    void paysPhyrexianWithManaWhenAvailable() {
        Permanent target = addReadyCreature(player2);
        harness.setHand(player1, List.of(new ActOfAggression()));
        // 3 colorless + 2 red — enough to pay entirely with mana
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        // Card resolved successfully, no life paid
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Pays partial Phyrexian mana with red and the rest with life")
    void paysPartialPhyrexianWithManaAndLife() {
        Permanent target = addReadyCreature(player2);
        harness.setHand(player1, List.of(new ActOfAggression()));
        // 3 colorless + 1 red — one Phyrexian paid with mana, one with 2 life
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        // Card resolved successfully
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        // Player paid 2 life (1 Phyrexian symbol paid with life)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent target = addReadyCreature(player2);
        harness.setHand(player1, List.of(new ActOfAggression()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
