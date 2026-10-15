package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.w.WoodenStake;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.m.MesmericOrb;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TraitorousBlood.class, WalkingCorpse.class, WoodenStake.class, MesmericOrb.class})
class TraitorousBloodTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Traitorous Blood untaps target, gains control, and grants trample and haste")
    void resolvesUntapGainControlTrampleAndHaste() {
        Permanent target = addCreatureReady(player2, new WalkingCorpse());
        target.tap();
        harness.setHand(player1, List.of(new TraitorousBlood()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isTrue();
    }

    @Test
    @DisplayName("Control, trample, and haste expire at cleanup")
    void controlTrampleAndHasteExpireAtCleanup() {
        Permanent target = addCreatureReady(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new TraitorousBlood()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
    }

    @Test
    @DisplayName("Stolen creature can attack this turn because Traitorous Blood grants haste")
    void stolenCreatureCanAttackDueToHaste() {
        Permanent target = addCreatureReady(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new TraitorousBlood()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(target);

        declareAttackers(player1, List.of(attackerIndex));

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new WoodenStake());
        harness.setHand(player1, List.of(new TraitorousBlood()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Traitorous Blood fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent target = addCreatureReady(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new TraitorousBlood()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("A creature already controlled by the caster is a legal target")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        target.tap();
        harness.setHand(player1, List.of(new TraitorousBlood()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Control changes before untapping, so Mesmeric Orb mills the caster")
    void gainsControlBeforeUntapTrigger() {
        harness.addToBattlefield(player1, new MesmericOrb());
        Permanent target = addCreatureReady(player2, new WalkingCorpse());
        target.tap();
        WalkingCorpse casterTopCard = new WalkingCorpse();
        WalkingCorpse opponentTopCard = new WalkingCorpse();
        harness.setLibrary(player1, List.of(casterTopCard, new WalkingCorpse()));
        harness.setLibrary(player2, List.of(opponentTopCard, new WalkingCorpse()));
        harness.setHand(player1, List.of(new TraitorousBlood()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, target.getId());
        harness.withAutoStop(gd.currentStep, this::resolveAllTriggers);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(casterTopCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(opponentTopCard);
        assertThat(gd.playerDecks.get(player2.getId())).contains(opponentTopCard);
    }
}
