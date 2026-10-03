package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Bloodthirster.class, GrizzlyBears.class, ChandraNalaar.class})
class BloodthirsterTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage creates one ability containing both instructions")
    void combatDamageCreatesOneAbility() {
        Permanent bloodthirster = addCreatureReady(player1, new Bloodthirster());

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> {
            declareAttackers(List.of(0));
            resolveCombat();
        });

        harness.assertLife(player2, 14);
        assertThat(bloodthirster.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Damage to a planeswalker does not untap Bloodthirster or add a combat")
    void planeswalkerDamageDoesNotTriggerAbility() {
        Permanent bloodthirster = addCreatureReady(player1, new Bloodthirster());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);
            harness.clearPriorityPassed();
            harness.beginAttackerDeclarationInput();
            gs.declareAttackers(gd, player1, List.of(0), Map.of(0, planeswalker.getId()));
            resolveCombat();
        });

        harness.assertLife(player2, 20);
        assertThat(bloodthirster.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    @Test
    @DisplayName("An additional combat is still created if Bloodthirster leaves before resolution")
    void sourceLeavingDoesNotPreventAdditionalCombat() {
        Permanent bloodthirster = addCreatureReady(player1, new Bloodthirster());
        addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> {
            declareAttackers(List.of(0));
            resolveCombat();
        });
        gd.playerBattlefields.get(player1.getId()).remove(bloodthirster);
        gd.playerGraveyards.get(player1.getId()).add(bloodthirster.getCard());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bloodthirster);
    }

    @Test
    @DisplayName("Combat damage untaps Bloodthirster and creates an additional combat")
    void combatDamageUntapsBloodthirsterAndCreatesAdditionalCombat() {
        Permanent bloodthirster = addCreatureReady(player1, new Bloodthirster());
        Permanent tappedBear = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        tappedBear.tap();

        declareAttackers(List.of(0));
        resolveCombat();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        assertThat(bloodthirster.isTapped()).isFalse();
        assertThat(tappedBear.isTapped()).isTrue();
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
    }

    @Test
    @DisplayName("Cannot attack the same player again, but another creature can")
    void cannotAttackPreviouslyAttackedPlayer() {
        Permanent bloodthirster = addCreatureReady(player1, new Bloodthirster());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.beginAttackerDeclarationInput();
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bloodthirster))))
                .isInstanceOf(IllegalStateException.class);

        gs.declareAttackers(gd, player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bear)));
    }

    @Test
    @DisplayName("Can attack a planeswalker controlled by a player already attacked")
    void canAttackPlaneswalkerControlledByPreviouslyAttackedPlayer() {
        Permanent bloodthirster = addCreatureReady(player1, new Bloodthirster());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.beginAttackerDeclarationInput();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> gs.declareAttackers(gd, player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bloodthirster)),
                Map.of(gd.playerBattlefields.get(player1.getId()).indexOf(bloodthirster), planeswalker.getId())));

        assertThat(bloodthirster.isAttacking()).isTrue();
        assertThat(bloodthirster.getAttackTarget()).isEqualTo(planeswalker.getId());
    }
}
