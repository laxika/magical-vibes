package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheMeep.class, ColossalDreadmaw.class, GrizzlyBears.class, LlanowarElves.class, Ornithopter.class, LightningBolt.class})
class TheMeepTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature sets your creatures' base power and toughness to its mana value")
    void sacrificingAnotherCreatureSetsBasePowerAndToughness() {
        Permanent meep = addCreatureReady(player1, new TheMeep());
        Permanent sacrificed = addCreatureReady(player1, new ColossalDreadmaw());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new LlanowarElves());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrificed.getId());

        assertThat(gqs.getEffectivePower(gd, meep)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, meep)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrificed.getCard());
    }

    @Test
    @DisplayName("Declining the sacrifice does nothing")
    void decliningSacrificeDoesNothing() {
        Permanent meep = addCreatureReady(player1, new TheMeep());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, meep)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, meep)).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("Accepting without another creature does nothing")
    void acceptingWithoutAnotherCreatureDoesNothing() {
        Permanent meep = addCreatureReady(player1, new TheMeep());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, meep)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, meep)).isEqualTo(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The base power and toughness change expires at end of turn")
    void basePowerAndToughnessChangeExpires() {
        Permanent meep = addCreatureReady(player1, new TheMeep());
        Permanent sacrificed = addCreatureReady(player1, new ColossalDreadmaw());
        Permanent other = addCreatureReady(player1, new LlanowarElves());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrificed.getId());

        assertThat(gqs.getEffectivePower(gd, meep)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, meep)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, meep)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creatures entering after resolution are not affected")
    void laterCreatureIsNotAffected() {
        Permanent meep = addCreatureReady(player1, new TheMeep());
        Permanent sacrificed = addCreatureReady(player1, new ColossalDreadmaw());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        Permanent laterCreature = harness.enterBattlefieldAndReturn(player1, new LlanowarElves());

        assertThat(gqs.getEffectivePower(gd, meep)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, meep)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, laterCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining to pay ward counters an opponent's spell")
    void decliningWardCountersOpponentsSpell() {
        Permanent meep = addCreatureReady(player1, new TheMeep());
        castOpponentsBoltAt(meep);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Lightning Bolt");
        assertThat(meep.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying ward costs 3 life and lets an opponent's spell resolve")
    void payingWardLetsOpponentsSpellResolve() {
        Permanent meep = addCreatureReady(player1, new TheMeep());
        castOpponentsBoltAt(meep);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player2, "Lightning Bolt");
        assertThat(meep.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing a zero-mana creature makes unmodified creatures die with zero toughness")
    void zeroManaSacrificeSetsZeroToughness() {
        addCreatureReady(player1, new TheMeep());
        Permanent sacrificed = addCreatureReady(player1, new Ornithopter());
        addCreatureReady(player1, new LlanowarElves());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrificed.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "The Meep");
        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("An opponent with fewer than 3 life cannot pay ward")
    void insufficientLifeCountersOpponentsSpell() {
        Permanent meep = addCreatureReady(player1, new TheMeep());
        harness.setLife(player2, 2);
        castOpponentsBoltAt(meep);

        harness.passBothPriorities();

        harness.assertLife(player2, 2);
        harness.assertInGraveyard(player2, "Lightning Bolt");
        assertThat(meep.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ward does not require payment for its controller's spell")
    void ownSpellDoesNotTriggerWard() {
        Permanent meep = addCreatureReady(player1, new TheMeep());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, meep.getId());

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Lightning Bolt");
        assertThat(meep.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }
    private void castOpponentsBoltAt(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, target.getId());
    }
}
