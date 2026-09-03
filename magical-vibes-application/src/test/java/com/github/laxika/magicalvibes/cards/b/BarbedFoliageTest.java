package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.m.MtendaGriffin;
import com.github.laxika.magicalvibes.cards.t.TelimTor;
import com.github.laxika.magicalvibes.cards.w.WildElephant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BarbedFoliage.class, MtendaGriffin.class, TelimTor.class, WildElephant.class})
class BarbedFoliageTest extends BaseCardTest {

    /** Puts Barbed Foliage on player1's battlefield and the given attacker on player2's. */
    private Permanent setUpAttack(Card attackerCard) {
        harness.addToBattlefield(player1, new BarbedFoliage());
        return addCreatureReady(player2, attackerCard);
    }

    @Test
    @DisplayName("A flanking attacker loses flanking until end of turn")
    void flankingAttackerLosesFlanking() {
        Permanent attacker = setUpAttack(new TelimTor());
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLANKING)).isTrue();

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLANKING)).isFalse();
    }

    @Test
    @DisplayName("Flanking returns after end of turn")
    void flankingReturnsAtEndOfTurn() {
        Permanent attacker = setUpAttack(new TelimTor());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLANKING)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLANKING)).isTrue();
    }

    @Test
    @DisplayName("A non-flying attacker takes 1 damage")
    void nonFlyingAttackerTakesOneDamage() {
        Permanent attacker = setUpAttack(new WildElephant());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("A flying attacker takes no damage")
    void flyingAttackerTakesNoDamage() {
        Permanent attacker = setUpAttack(new MtendaGriffin());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Each non-flying attacker takes 1 damage")
    void eachNonFlyingAttackerTakesOneDamage() {
        harness.addToBattlefield(player1, new BarbedFoliage());
        Permanent firstAttacker = addCreatureReady(player2, new WildElephant());
        Permanent secondAttacker = addCreatureReady(player2, new WildElephant());

        declareAttackers(player2, List.of(0, 1));
        resolveAllTriggers();

        assertThat(firstAttacker.getMarkedDamage()).isEqualTo(1);
        assertThat(secondAttacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when a creature attacks a planeswalker you control")
    void doesNotTriggerWhenPlaneswalkerAttacked() {
        harness.addToBattlefield(player1, new BarbedFoliage());

        Card planeswalkerCard = new Card();
        planeswalkerCard.setName("Test Planeswalker");
        planeswalkerCard.setType(CardType.PLANESWALKER);
        planeswalkerCard.setLoyalty(3);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, planeswalkerCard);
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);

        Permanent attacker = addCreatureReady(player2, new TelimTor());
        declareAttackingPlaneswalker(planeswalker);
        resolveAllTriggers();

        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLANKING)).isTrue();
    }

    private void declareAttackingPlaneswalker(Permanent planeswalker) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(0), Map.of(0, planeswalker.getId()));
    }
}
