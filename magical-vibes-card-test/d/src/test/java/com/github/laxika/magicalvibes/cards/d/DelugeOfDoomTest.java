package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DelugeOfDoom.class, AirElemental.class, Forest.class, GrizzlyBears.class, HillGiant.class,
        Ornithopter.class, Pacifism.class, Shock.class})
class DelugeOfDoomTest extends BaseCardTest {

    @Test
    @DisplayName("Gives all creatures -3/-3 for three distinct card types in the controller's graveyard")
    void givesAllCreaturesMinusThreeMinusThree() {
        Permanent ownCreature = addCreatureReady(player1, new AirElemental());
        Permanent opponentCreature = addCreatureReady(player2, new AirElemental());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));

        castDelugeOfDoom();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts each card type once and ignores the opponent's graveyard")
    void countsDistinctTypesInOwnGraveyardOnly() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new Forest(), new Shock(), new Pacifism()));

        castDelugeOfDoom();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("The debuff wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player2, new HillGiant());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest()));

        castDelugeOfDoom();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("An empty graveyard gives no reduction and the resolving spell does not count itself")
    void emptyGraveyardDoesNotCountResolvingSpell() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of());

        castDelugeOfDoom();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Deluge of Doom");
    }

    @Test
    @DisplayName("An artifact creature contributes both card types")
    void countsAllTypesOfMultitypeCards() {
        Permanent creature = addCreatureReady(player2, new AirElemental());
        harness.setGraveyard(player1, List.of(new Ornithopter(), new GrizzlyBears()));

        castDelugeOfDoom();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures reduced to zero toughness die on both battlefields")
    void killsCreaturesOnBothBattlefields() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Forest(), new Shock()));

        castDelugeOfDoom();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Counts types at resolution and keeps the amount fixed afterward")
    void countsAtResolutionAndLocksInAmount() {
        Permanent creature = addCreatureReady(player2, new AirElemental());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.castFromHand(player1, new DelugeOfDoom(), "{2}{B}");
        harness.setGraveyard(player1, List.of(new Forest(), new Shock()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        harness.setGraveyard(player1, List.of());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering after resolution are not affected")
    void doesNotAffectLaterCreatures() {
        Permanent existingCreature = addCreatureReady(player2, new AirElemental());
        harness.setGraveyard(player1, List.of(new Forest(), new Shock()));

        castDelugeOfDoom();
        Permanent laterCreature = harness.enterBattlefieldAndReturn(player2, new AirElemental());

        assertThat(gqs.getEffectivePower(gd, existingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, existingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, laterCreature)).isEqualTo(4);
    }

    private void castDelugeOfDoom() {
        harness.castFromHand(player1, new DelugeOfDoom(), "{2}{B}");
        harness.passBothPriorities();
    }
}
