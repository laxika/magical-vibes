package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.t.TovolarsMagehunter;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.NoSpellsCastLastTurnConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TwoOrMoreSpellsCastLastTurnConditionalEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MondronenShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Front face has the no-spells upkeep transform trigger")
    void frontFaceHasCorrectEffects() {
        MondronenShaman card = new MondronenShaman();

        assertThat(card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(NoSpellsCastLastTurnConditionalEffect.class);
        NoSpellsCastLastTurnConditionalEffect conditional =
                (NoSpellsCastLastTurnConditionalEffect) card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst();
        assertThat(conditional.wrapped()).isInstanceOf(TransformSelfEffect.class);

        assertThat(card.getBackFaceCard()).isInstanceOf(TovolarsMagehunter.class);
        assertThat(card.getBackFaceClassName()).isEqualTo("TovolarsMagehunter");
    }

    @Test
    @DisplayName("Back face damages opponents who cast spells and has the two-spells transform trigger")
    void backFaceHasCorrectEffects() {
        MondronenShaman card = new MondronenShaman();
        TovolarsMagehunter backFace = (TovolarsMagehunter) card.getBackFaceCard();

        assertThat(backFace.getEffects(EffectSlot.ON_OPPONENT_CASTS_SPELL)).hasSize(1);
        assertThat(backFace.getEffects(EffectSlot.ON_OPPONENT_CASTS_SPELL).getFirst())
                .isInstanceOf(DealDamageToTargetPlayerEffect.class);
        DealDamageToTargetPlayerEffect damage =
                (DealDamageToTargetPlayerEffect) backFace.getEffects(EffectSlot.ON_OPPONENT_CASTS_SPELL).getFirst();
        assertThat(damage.damage()).isEqualTo(2);

        assertThat(backFace.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(backFace.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(TwoOrMoreSpellsCastLastTurnConditionalEffect.class);
    }

    @Test
    @DisplayName("Transforms to Tovolar's Magehunter when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        harness.addToBattlefield(player1, new MondronenShaman());
        Permanent shaman = findPermanent(player1, "Mondronen Shaman");

        gd.spellsCastLastTurn.clear();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(shaman.isTransformed()).isTrue();
        assertThat(shaman.getCard().getName()).isEqualTo("Tovolar's Magehunter");
        assertThat(gqs.getEffectivePower(gd, shaman)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, shaman)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not transform when any spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        harness.addToBattlefield(player1, new MondronenShaman());
        Permanent shaman = findPermanent(player1, "Mondronen Shaman");

        gd.spellsCastLastTurn.put(player2.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(shaman.isTransformed()).isFalse();
        assertThat(shaman.getCard().getName()).isEqualTo("Mondronen Shaman");
    }

    @Test
    @DisplayName("Tovolar's Magehunter transforms back when a player cast two or more spells last turn")
    void magehunterTransformsBackWhenTwoSpellsCast() {
        Permanent shaman = addTransformedMagehunter();

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(shaman.isTransformed()).isFalse();
        assertThat(shaman.getCard().getName()).isEqualTo("Mondronen Shaman");
        assertThat(gqs.getEffectivePower(gd, shaman)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, shaman)).isEqualTo(2);
    }

    @Test
    @DisplayName("Tovolar's Magehunter does not transform back when each player cast only one spell")
    void magehunterDoesNotTransformBackWhenOnlyOneSpellEach() {
        Permanent shaman = addTransformedMagehunter();

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(shaman.isTransformed()).isTrue();
        assertThat(shaman.getCard().getName()).isEqualTo("Tovolar's Magehunter");
    }

    @Test
    @DisplayName("Tovolar's Magehunter deals 2 damage to an opponent who casts a spell")
    void magehunterDamagesOpponentWhoCastsSpell() {
        addTransformedMagehunter();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(player2.getId());

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Tovolar's Magehunter does not trigger when its controller casts a spell")
    void magehunterDoesNotDamageControllerForOwnSpell() {
        addTransformedMagehunter();

        harness.setHand(player1, List.of(new SuntailHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    private Permanent addTransformedMagehunter() {
        harness.addToBattlefield(player1, new MondronenShaman());
        Permanent shaman = findPermanent(player1, "Mondronen Shaman");
        shaman.setCard(shaman.getOriginalCard().getBackFaceCard());
        shaman.setTransformed(true);
        return shaman;
    }
}
