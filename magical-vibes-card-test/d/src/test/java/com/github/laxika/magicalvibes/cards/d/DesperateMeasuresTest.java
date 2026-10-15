package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ovinize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DesperateMeasures.class, DoomBlade.class, GrizzlyBears.class,
        DescendantOfStorms.class, Ovinize.class})
class DesperateMeasuresTest extends BaseCardTest {

    @Test
    void boostsTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castOn(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    void drawsTwoCardsWhenTargetedCreatureYouControlDies() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castOn(creature);
        destroy(player2, creature);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void opponentDrawsWhenTheirTargetedCreatureDies() {
        harness.setHand(player2, List.of());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castOn(creature);
        destroy(player1, creature);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    void deathTriggerWearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castOn(creature);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        destroy(player2, creature);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void powerAndToughnessChangeExpiresAtEndOfTurn() {
        Permanent survivingCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castOn(survivingCreature);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, survivingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, survivingCreature)).isEqualTo(2);
    }

    @Test
    void drawsWhenMinusOneToughnessKillsTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DescendantOfStorms());
        harness.setLibrary(player1, List.of(new DescendantOfStorms(), new DescendantOfStorms()));

        castOn(creature);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Descendant of Storms");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void delayedDrawSurvivesTargetLosingAllAbilities() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        castOn(creature);
        harness.setHand(player2, List.of(new Ovinize()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void doesNotDrawWhenTargetDiesBeforeSpellResolves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DesperateMeasures()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, creature.getId());

        destroy(player2, creature);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    private void castOn(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DesperateMeasures()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void destroy(com.github.laxika.magicalvibes.model.Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new DoomBlade()));
        harness.addMana(caster, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(caster, 0, target.getId());
    }
}
