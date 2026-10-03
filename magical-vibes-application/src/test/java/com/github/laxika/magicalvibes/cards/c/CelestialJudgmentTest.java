package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.u.UnbreakableFormation;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CelestialJudgment.class, AirElemental.class, GrizzlyBears.class, HillGiant.class,
        LlanowarElves.class, UnbreakableFormation.class})
class CelestialJudgmentTest extends BaseCardTest {

    @Test
    void choosesOneCreatureForEachDistinctPowerAndDestroysTheRest() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new AirElemental());

        castCelestialJudgment();

        PendingInteraction.PermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(firstChoice.validPermanentIds()).containsExactly(
                harness.getPermanentId(player1, "Grizzly Bears"),
                harness.getPermanentId(player2, "Grizzly Bears"));
        harness.handlePermanentChosen(player1, firstChoice.validPermanentIds().getFirst());

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    void withNoCreaturesThereIsNoChoice() {
        castCelestialJudgment();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Celestial Judgment");
    }

    @Test
    void waitsForEveryPowerGroupBeforeDestroyingAnyCreature() {
        Permanent ownElf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent opposingElf = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castCelestialJudgment();

        PendingInteraction.PermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        assertThat(firstChoice.validPermanentIds()).containsExactlyInAnyOrder(ownElf.getId(), opposingElf.getId());
        harness.handlePermanentChosen(player1, opposingElf.getId());

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player2, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        PendingInteraction.PermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(secondChoice.playerId()).isEqualTo(player1.getId());
        assertThat(secondChoice.validPermanentIds()).containsExactlyInAnyOrder(ownBear.getId(), opposingBear.getId());
        harness.handlePermanentChosen(player1, ownBear.getId());

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player2, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Celestial Judgment");
    }

    @Test
    void groupsCreaturesByPowerIncludingCounters() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        elf.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castCelestialJudgment();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(elf.getId(), bear.getId());
        harness.handlePermanentChosen(player1, bear.getId());

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void distinguishesNegativePowerFromZeroPower() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        elf.setPowerModifier(-2);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bear.setPowerModifier(-2);

        castCelestialJudgment();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void unchosenIndestructibleCreatureSurvivesAlongsideChosenCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.castFromHand(player1, new UnbreakableFormation(), "{2}{W}");
        harness.passBothPriorities();
        Permanent chosenGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent otherGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castCelestialJudgment();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(
                harness.getPermanentId(player1, "Grizzly Bears"), chosenGiant.getId(), otherGiant.getId());
        harness.handlePermanentChosen(player1, chosenGiant.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(chosenGiant);
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    void unchosenCreatureCanRegenerate() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opposingBear.setRegenerationShield(1);

        castCelestialJudgment();
        harness.handlePermanentChosen(player1, ownBear.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(opposingBear.isTapped()).isTrue();
        assertThat(opposingBear.getRegenerationShield()).isZero();
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    private void castCelestialJudgment() {
        harness.castFromHand(player1, new CelestialJudgment(), "{4}{W}{W}");
        harness.passBothPriorities();
    }
}
