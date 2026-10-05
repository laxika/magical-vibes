package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FaeburrowElder;
import com.github.laxika.magicalvibes.cards.f.Frogify;
import com.github.laxika.magicalvibes.cards.g.GarenbrigSquire;
import com.github.laxika.magicalvibes.cards.o.OnAlert;
import com.github.laxika.magicalvibes.cards.s.ShepherdOfTheFlock;
import com.github.laxika.magicalvibes.cards.s.SilverflameSquire;
import com.github.laxika.magicalvibes.cards.u.UsherToSafety;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LindenTheSteadfastQueen.class, FaeburrowElder.class, Frogify.class, GarenbrigSquire.class, OnAlert.class, ShepherdOfTheFlock.class, SilverflameSquire.class, UsherToSafety.class})
class LindenTheSteadfastQueenTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life for each white creature that attacks")
    void gainsLifeForEachWhiteCreatureThatAttacks() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new LindenTheSteadfastQueen());
        addCreatureReady(player1, creature(CardColor.WHITE, 2));
        addCreatureReady(player1, creature(CardColor.WHITE, 2));

        declareAttackers(List.of(0, 1, 2));

        assertThat(gd.stack).hasSize(3);
        assertThat(gd.stack).allSatisfy(entry ->
                assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY));

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Does not trigger for a nonwhite creature that attacks")
    void doesNotTriggerForNonwhiteCreature() {
        addCreatureReady(player1, new LindenTheSteadfastQueen());
        addCreatureReady(player1, creature(CardColor.GREEN, 2));

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for an opponent's white creature")
    void doesNotTriggerForOpponentsWhiteCreature() {
        addCreatureReady(player1, new LindenTheSteadfastQueen());
        addCreatureReady(player2, creature(CardColor.WHITE, 2));

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void multicoloredWhiteAttackerTriggersOnceWhileLindenDoesNotAttack() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new LindenTheSteadfastQueen());
        addCreatureReady(player1, new FaeburrowElder());

        declareAttackers(List.of(1));

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    void mixedAttackOnlyGainsLifeForWhiteAttackers() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new LindenTheSteadfastQueen());
        addCreatureReady(player1, new SilverflameSquire());
        addCreatureReady(player1, new GarenbrigSquire());

        declareAttackers(List.of(0, 1, 2));

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    void lifeGainResolvesAfterAttackingLindenReturnsToHand() {
        harness.setLife(player1, 20);
        Permanent linden = addCreatureReady(player1, new LindenTheSteadfastQueen());
        harness.setHand(player1, List.of(new ShepherdOfTheFlock()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        harness.castAdventure(player1, 0, linden.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(linden);
        assertThat(gd.playerHands.get(player1.getId())).contains(linden.getCard());
        resolveAllTriggers();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    void formerlyWhiteAttackerMadeBlueDoesNotTrigger() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new LindenTheSteadfastQueen());
        Permanent squire = addCreatureReady(player1, new SilverflameSquire());
        harness.setHand(player1, List.of(new Frogify()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, squire.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void lindenWithNoAbilitiesDoesNotTriggerForAnotherWhiteAttacker() {
        harness.setLife(player1, 20);
        Permanent linden = addCreatureReady(player1, new LindenTheSteadfastQueen());
        addCreatureReady(player1, new SilverflameSquire());
        harness.setHand(player1, List.of(new Frogify()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, linden.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    private Card creature(CardColor color, int power) {
        Card creature = new Card();
        creature.setName("Test Creature");
        creature.setType(CardType.CREATURE);
        creature.setColors(List.of(color));
        creature.setPower(power);
        creature.setToughness(power);
        return creature;
    }
}
