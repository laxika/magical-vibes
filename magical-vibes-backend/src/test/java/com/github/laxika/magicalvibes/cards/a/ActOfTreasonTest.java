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

class ActOfTreasonTest extends BaseCardTest {

    @Test
    @DisplayName("Act of Treason has correct card properties")
    void hasCorrectProperties() {
        ActOfTreason card = new ActOfTreason();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(3);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(UntapTargetPermanentEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(GainControlOfTargetPermanentUntilEndOfTurnEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(2)).isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect effect = (GrantKeywordEffect) card.getEffects(EffectSlot.SPELL).get(2);
        assertThat(effect.keyword()).isEqualTo(Keyword.HASTE);
        assertThat(effect.scope()).isEqualTo(GrantScope.TARGET);
    }

    @Test
    @DisplayName("Casting Act of Treason puts it on the stack with the target creature")
    void castingPutsOnStack() {
        Permanent target = addReadyCreature(player2);
        harness.setHand(player1, List.of(new ActOfTreason()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Act of Treason");
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Resolving Act of Treason untaps target, gains control, and grants haste")
    void resolvesUntapGainControlAndHaste() {
        Permanent target = addReadyCreature(player2);
        target.tap();
        harness.setHand(player1, List.of(new ActOfTreason()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.untilEndOfTurnStolenCreatures).contains(target.getId());
    }

    @Test
    @DisplayName("Stolen creature can attack this turn because Act of Treason grants haste")
    void stolenCreatureCanAttackDueToHaste() {
        Permanent target = addReadyCreature(player2);
        target.setSummoningSick(false);
        harness.setHand(player1, List.of(new ActOfTreason()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, target.getId());
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
    @DisplayName("Act of Treason control and haste expire at cleanup")
    void controlAndHasteExpireAtCleanup() {
        Permanent target = addReadyCreature(player2);
        harness.setHand(player1, List.of(new ActOfTreason()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, target.getId());
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
    @DisplayName("Can target own creature (control change is a no-op)")
    void canTargetOwnCreature() {
        Permanent ownCreature = addReadyCreature(player1);
        ownCreature.tap();
        harness.setHand(player1, List.of(new ActOfTreason()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(ownCreature.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.untilEndOfTurnStolenCreatures).doesNotContain(ownCreature.getId());
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addReadyCreature(player1); // valid target so spell is playable
        Permanent enchantment = new Permanent(new Pacifism());
        gd.playerBattlefields.get(player2.getId()).add(enchantment);
        harness.setHand(player1, List.of(new ActOfTreason()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Act of Treason fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent target = addReadyCreature(player2);
        harness.setHand(player1, List.of(new ActOfTreason()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, target.getId());
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
