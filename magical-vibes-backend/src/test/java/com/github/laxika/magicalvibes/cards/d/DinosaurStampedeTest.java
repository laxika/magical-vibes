package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DinosaurStampedeTest extends BaseCardTest {

    @Test
    @DisplayName("Dinosaur Stampede has correct effects configured")
    void hasCorrectEffects() {
        DinosaurStampede card = new DinosaurStampede();

        assertThat(card.isNeedsTarget()).isFalse();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(BoostAllCreaturesEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(GrantKeywordEffect.class);
    }

    @Test
    @DisplayName("Attacking Dinosaur gets +2/+0 and trample")
    void attackingDinosaurGetsBothEffects() {
        Permanent dino = addReadyCreature(player1, new DeathgorgeScavenger()); // 3/2 Dinosaur
        dino.setAttacking(true);

        harness.setHand(player1, List.of(new DinosaurStampede()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0);

        assertThat(dino.getEffectivePower()).isEqualTo(5);  // 3 + 2
        assertThat(dino.getEffectiveToughness()).isEqualTo(2);
        assertThat(dino.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Attacking non-Dinosaur gets +2/+0 but not trample")
    void attackingNonDinosaurGetsBoostOnly() {
        Permanent bear = addReadyCreature(player1, new GrizzlyBears()); // 2/2 Bear
        bear.setAttacking(true);

        harness.setHand(player1, List.of(new DinosaurStampede()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0);

        assertThat(bear.getEffectivePower()).isEqualTo(4);  // 2 + 2
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
        assertThat(bear.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Non-attacking Dinosaur gets trample but not +2/+0")
    void nonAttackingDinosaurGetsTrampleOnly() {
        Permanent dino = addReadyCreature(player1, new DeathgorgeScavenger()); // 3/2 Dinosaur

        harness.setHand(player1, List.of(new DinosaurStampede()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0);

        assertThat(dino.getEffectivePower()).isEqualTo(3);  // unchanged
        assertThat(dino.getEffectiveToughness()).isEqualTo(2);
        assertThat(dino.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Non-attacking non-Dinosaur gets nothing")
    void nonAttackingNonDinosaurGetsNothing() {
        Permanent bear = addReadyCreature(player1, new GrizzlyBears()); // 2/2 Bear

        harness.setHand(player1, List.of(new DinosaurStampede()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0);

        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
        assertThat(bear.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Opponent's Dinosaurs do not gain trample")
    void opponentDinosaursDoNotGainTrample() {
        Permanent ownDino = addReadyCreature(player1, new DeathgorgeScavenger());
        ownDino.setAttacking(true);
        Permanent opponentDino = addReadyCreature(player2, new DeathgorgeScavenger());

        harness.setHand(player1, List.of(new DinosaurStampede()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0);

        assertThat(ownDino.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(opponentDino.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent dino = addReadyCreature(player1, new DeathgorgeScavenger());
        dino.setAttacking(true);

        harness.setHand(player1, List.of(new DinosaurStampede()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0);

        assertThat(dino.getEffectivePower()).isEqualTo(5);
        assertThat(dino.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(dino.getEffectivePower()).isEqualTo(3);
        assertThat(dino.getEffectiveToughness()).isEqualTo(2);
        assertThat(dino.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
