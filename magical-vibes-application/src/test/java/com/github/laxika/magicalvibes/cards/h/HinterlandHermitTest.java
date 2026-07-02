package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleEffect;
import com.github.laxika.magicalvibes.model.condition.NoSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.condition.TwoOrMoreSpellsCastLastTurn;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HinterlandHermitTest extends BaseCardTest {

    @Test
    @DisplayName("Front face has correct transform trigger configured")
    void frontFaceHasCorrectEffects() {
        HinterlandHermit card = new HinterlandHermit();

        assertThat(card.getActivatedAbilities()).isEmpty();
        assertThat(card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(ConditionalEffect.class);
        ConditionalEffect conditional =
                (ConditionalEffect) card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst();
        assertThat(conditional.wrapped()).isInstanceOf(TransformSelfEffect.class);

        assertThat(card.getBackFaceCard()).isInstanceOf(HinterlandScourge.class);
        assertThat(card.getBackFaceClassName()).isEqualTo("HinterlandScourge");
    }

    @Test
    @DisplayName("Back face has correct static ability and transform trigger configured")
    void backFaceHasCorrectEffects() {
        HinterlandHermit card = new HinterlandHermit();
        HinterlandScourge backFace = (HinterlandScourge) card.getBackFaceCard();

        assertThat(backFace.getActivatedAbilities()).isEmpty();
        assertThat(backFace.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(backFace.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(MustBeBlockedIfAbleEffect.class);
        assertThat(backFace.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(backFace.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(ConditionalEffect.class);
        ConditionalEffect conditional =
                (ConditionalEffect) backFace.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst();
        assertThat(conditional.wrapped()).isInstanceOf(TransformSelfEffect.class);
    }

    @Test
    @DisplayName("Transforms to Hinterland Scourge when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        harness.addToBattlefield(player1, new HinterlandHermit());
        Permanent hermit = findPermanent(player1, "Hinterland Hermit");

        gd.spellsCastLastTurn.clear();
        advanceFromUntapToResolveUpkeepTrigger(player1);

        assertThat(hermit.isTransformed()).isTrue();
        assertThat(hermit.getCard().getName()).isEqualTo("Hinterland Scourge");
        assertThat(gqs.getEffectivePower(gd, hermit)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hermit)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        harness.addToBattlefield(player1, new HinterlandHermit());
        Permanent hermit = findPermanent(player1, "Hinterland Hermit");

        gd.spellsCastLastTurn.put(player1.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(hermit.isTransformed()).isFalse();
        assertThat(hermit.getCard().getName()).isEqualTo("Hinterland Hermit");
    }

    @Test
    @DisplayName("Hinterland Scourge transforms back when a player cast two or more spells last turn")
    void transformsBackWhenTwoSpellsCastLastTurn() {
        harness.addToBattlefield(player1, new HinterlandHermit());
        Permanent hermit = findPermanent(player1, "Hinterland Hermit");

        gd.spellsCastLastTurn.clear();
        advanceFromUntapToResolveUpkeepTrigger(player1);
        assertThat(hermit.isTransformed()).isTrue();

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        advanceFromUntapToResolveUpkeepTrigger(player2);

        assertThat(hermit.isTransformed()).isFalse();
        assertThat(hermit.getCard().getName()).isEqualTo("Hinterland Hermit");
        assertThat(gqs.getEffectivePower(gd, hermit)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hermit)).isEqualTo(1);
    }

    @Test
    @DisplayName("Hinterland Scourge does not transform back when no player cast two spells last turn")
    void doesNotTransformBackWithOnlyOneSpellCastLastTurn() {
        harness.addToBattlefield(player1, new HinterlandHermit());
        Permanent hermit = findPermanent(player1, "Hinterland Hermit");

        gd.spellsCastLastTurn.clear();
        advanceFromUntapToResolveUpkeepTrigger(player1);
        assertThat(hermit.isTransformed()).isTrue();

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(hermit.isTransformed()).isTrue();
        assertThat(hermit.getCard().getName()).isEqualTo("Hinterland Scourge");
    }

    @Test
    @DisplayName("Hinterland Scourge must be blocked if able")
    void hinterlandScourgeMustBeBlockedIfAble() {
        Permanent scourge = attackingCreature(new HinterlandScourge());
        gd.playerBattlefields.get(player1.getId()).add(scourge);
        gd.playerBattlefields.get(player2.getId()).add(readyCreature(new GrizzlyBears()));

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked if able");
    }

    @Test
    @DisplayName("One blocker satisfies Hinterland Scourge's blocking requirement")
    void oneBlockerSatisfiesHinterlandScourgeRequirement() {
        Permanent scourge = attackingCreature(new HinterlandScourge());
        gd.playerBattlefields.get(player1.getId()).add(scourge);
        gd.playerBattlefields.get(player2.getId()).add(readyCreature(new GrizzlyBears()));
        gd.playerBattlefields.get(player2.getId()).add(readyCreature(new GrizzlyBears()));

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Tapped creatures are not forced to block Hinterland Scourge")
    void tappedCreaturesAreNotForcedToBlockHinterlandScourge() {
        Permanent scourge = attackingCreature(new HinterlandScourge());
        gd.playerBattlefields.get(player1.getId()).add(scourge);
        Permanent tapped = readyCreature(new GrizzlyBears());
        tapped.tap();
        gd.playerBattlefields.get(player2.getId()).add(tapped);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());
    }

    private void advanceFromUntapToResolveUpkeepTrigger(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent attackingCreature(Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        return permanent;
    }

    private Permanent readyCreature(Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void prepareDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);
    }
}
