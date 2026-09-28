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
