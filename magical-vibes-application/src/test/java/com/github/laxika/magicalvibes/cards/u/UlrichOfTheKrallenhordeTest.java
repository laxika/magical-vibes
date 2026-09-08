package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KrallenhordeWantons;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UlrichOfTheKrallenhordeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives target creature +4/+4 until end of turn")
    void etbBoostsTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UlrichOfTheKrallenhorde()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
    }

    @Test
    @DisplayName("Transforms to Ulrich, Uncontested Alpha when no spells were cast last turn")
    void transformsToBackFaceWhenNoSpellsWereCast() {
        Permanent ulrich = addReadyUlrich(player1);
        gd.spellsCastLastTurn.clear();

        advanceToUpkeepAndResolveTransform(player1);

        assertThat(ulrich.isTransformed()).isTrue();
        assertThat(ulrich.getCard().getName()).isEqualTo("Ulrich, Uncontested Alpha");
    }

    @Test
    @DisplayName("Back-face transform trigger optionally makes Ulrich fight a non-Werewolf creature an opponent controls")
    void backFaceTransformTriggerFightsTarget() {
        Permanent ulrich = addReadyUlrich(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.spellsCastLastTurn.clear();

        advanceToUpkeepAndResolveTransform(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(6);
        assertThat(ulrich.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Back-face transform trigger can be declined")
    void backFaceTransformTriggerCanBeDeclined() {
        Permanent ulrich = addReadyUlrich(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.spellsCastLastTurn.clear();

        advanceToUpkeepAndResolveTransform(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(ulrich.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Back-face transform trigger cannot target a Werewolf or a creature controlled by Ulrich's controller")
    void backFaceTransformTriggerRestrictsTarget() {
        addReadyUlrich(player1);
        Permanent werewolf = harness.addToBattlefieldAndReturn(player2, new KrallenhordeWantons());
        Permanent legalTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.spellsCastLastTurn.clear();

        advanceToUpkeepAndResolveTransform(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, werewolf.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, legalTarget.getId());
    }

    @Test
    @DisplayName("Transforms back to Ulrich of the Krallenhorde when a player cast two or more spells last turn")
    void transformsBackWhenTwoSpellsWereCast() {
        Permanent ulrich = addReadyBackFaceUlrich(player1);
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        advanceToUpkeepAndResolveTransform(player2);

        assertThat(ulrich.isTransformed()).isFalse();
        assertThat(ulrich.getCard().getName()).isEqualTo("Ulrich of the Krallenhorde");
    }

    @Test
    @DisplayName("Transforming back to Ulrich of the Krallenhorde gives a target creature +4/+4")
    void frontFaceTransformTriggerBoostsTargetCreature() {
        Permanent ulrich = addReadyBackFaceUlrich(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        advanceToUpkeepAndResolveTransform(player2);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(ulrich.isTransformed()).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
    }

    @Test
    @DisplayName("Does not transform back when only one spell was cast last turn")
    void doesNotTransformBackAfterOnlyOneSpell() {
        Permanent ulrich = addReadyBackFaceUlrich(player1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ulrich.isTransformed()).isTrue();
        assertThat(ulrich.getCard().getName()).isEqualTo("Ulrich, Uncontested Alpha");
    }

    private Permanent addReadyUlrich(Player player) {
        Permanent ulrich = new Permanent(new UlrichOfTheKrallenhorde());
        ulrich.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(ulrich);
        return ulrich;
    }

    private Permanent addReadyBackFaceUlrich(Player player) {
        UlrichOfTheKrallenhorde frontFace = new UlrichOfTheKrallenhorde();
        Permanent ulrich = new Permanent(frontFace);
        ulrich.setCard(frontFace.getBackFaceCard());
        ulrich.setTransformed(true);
        ulrich.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(ulrich);
        return ulrich;
    }

    private void advanceToUpkeepAndResolveTransform(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
