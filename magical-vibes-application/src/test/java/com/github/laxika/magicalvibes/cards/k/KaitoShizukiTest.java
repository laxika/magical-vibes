package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PhantomWarrior;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KaitoShizuki.class, GrizzlyBears.class, PhantomWarrior.class})
class KaitoShizukiTest extends BaseCardTest {

    @Test
    @DisplayName("Phases out at your end step when Kaito entered this turn")
    void phasesOutWhenEnteredThisTurn() {
        Permanent kaito = harness.enterBattlefieldAndReturn(player1, new KaitoShizuki());
        kaito.setCounterCount(CounterType.LOYALTY, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(kaito);
    }

    @Test
    @DisplayName("Does not phase out at your end step when Kaito entered earlier")
    void doesNotPhaseOutWhenEnteredEarlier() {
        Permanent kaito = addReadyKaito(3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kaito);
        assertThat(gd.phasedOutPermanents.getOrDefault(player1.getId(), List.of())).doesNotContain(kaito);
    }

    @Test
    @DisplayName("+1 draws and discards when you did not attack")
    void plusOneDrawsAndDiscardsWithoutAttack() {
        addReadyKaito(3);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new PhantomWarrior()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Phantom Warrior");
    }

    @Test
    @DisplayName("+1 skips the discard when you attacked")
    void plusOneSkipsDiscardAfterAttack() {
        addReadyKaito(3);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new PhantomWarrior()));
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears", "Phantom Warrior");
    }

    @Test
    @DisplayName("-2 creates a 1/1 blue Ninja that can't be blocked")
    void minusTwoCreatesUnblockableNinja() {
        addReadyKaito(3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent ninja = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(ninja.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(ninja.getCard().getSubtypes()).contains(CardSubtype.NINJA);
        assertThat(gqs.getEffectivePower(gd, ninja)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ninja)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, ninja)).isTrue();
    }

    @Test
    @DisplayName("-7 emblem searches for a blue or black creature after combat damage")
    void minusSevenEmblemSearchesAfterCombatDamage() {
        Permanent kaito = addReadyKaito(7);
        Permanent attacker = addReadyCreature(new GrizzlyBears());
        harness.setLibrary(player1, List.of(new PhantomWarrior()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.emblems).hasSize(1);
        Emblem emblem = gd.emblems.getFirst();
        assertThat(emblem.controllerId()).isEqualTo(player1.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(kaito);

        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Phantom Warrior")).hasSize(1);
    }

    private Permanent addReadyKaito(int loyalty) {
        Permanent kaito = new Permanent(new KaitoShizuki());
        kaito.setCounterCount(CounterType.LOYALTY, loyalty);
        kaito.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(kaito);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return kaito;
    }

    private Permanent addReadyCreature(Card card) {
        Permanent creature = new Permanent(card);
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(creature);
        return creature;
    }
}
