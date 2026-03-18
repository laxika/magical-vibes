package com.github.laxika.magicalvibes.cards.t;

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

class TraitorousBloodTest extends BaseCardTest {

    @Test
    @DisplayName("Traitorous Blood has correct card properties")
    void hasCorrectProperties() {
        TraitorousBlood card = new TraitorousBlood();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(4);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(UntapTargetPermanentEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(GainControlOfTargetPermanentUntilEndOfTurnEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(2)).isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect trampleEffect = (GrantKeywordEffect) card.getEffects(EffectSlot.SPELL).get(2);
        assertThat(trampleEffect.keyword()).isEqualTo(Keyword.TRAMPLE);
        assertThat(trampleEffect.scope()).isEqualTo(GrantScope.TARGET);
        assertThat(card.getEffects(EffectSlot.SPELL).get(3)).isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect hasteEffect = (GrantKeywordEffect) card.getEffects(EffectSlot.SPELL).get(3);
        assertThat(hasteEffect.keyword()).isEqualTo(Keyword.HASTE);
        assertThat(hasteEffect.scope()).isEqualTo(GrantScope.TARGET);
    }

    @Test
    @DisplayName("Resolving Traitorous Blood untaps target, gains control, and grants trample and haste")
    void resolvesUntapGainControlTrampleAndHaste() {
        Permanent target = addReadyCreature(player2);
        target.tap();
        harness.setHand(player1, List.of(new TraitorousBlood()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.untilEndOfTurnStolenCreatures).contains(target.getId());
    }

    @Test
    @DisplayName("Control, trample, and haste expire at cleanup")
    void controlTrampleAndHasteExpireAtCleanup() {
        Permanent target = addReadyCreature(player2);
        harness.setHand(player1, List.of(new TraitorousBlood()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.untilEndOfTurnStolenCreatures).doesNotContain(target.getId());
    }

    @Test
    @DisplayName("Stolen creature can attack this turn because Traitorous Blood grants haste")
    void stolenCreatureCanAttackDueToHaste() {
        Permanent target = addReadyCreature(player2);
        target.setSummoningSick(false);
        harness.setHand(player1, List.of(new TraitorousBlood()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

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
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addReadyCreature(player1);
        Permanent enchantment = new Permanent(new Pacifism());
        gd.playerBattlefields.get(player2.getId()).add(enchantment);
        harness.setHand(player1, List.of(new TraitorousBlood()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Traitorous Blood fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent target = addReadyCreature(player2);
        harness.setHand(player1, List.of(new TraitorousBlood()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

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
