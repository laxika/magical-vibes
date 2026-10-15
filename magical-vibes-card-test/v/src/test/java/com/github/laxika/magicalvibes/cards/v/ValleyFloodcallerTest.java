package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BloomingBlast;
import com.github.laxika.magicalvibes.cards.d.DruidOfTheSpade;
import com.github.laxika.magicalvibes.cards.f.FinchFormation;
import com.github.laxika.magicalvibes.cards.f.FountainportBell;
import com.github.laxika.magicalvibes.cards.k.KitsaOtterballElite;
import com.github.laxika.magicalvibes.cards.m.MindSpiral;
import com.github.laxika.magicalvibes.cards.p.PondProphet;
import com.github.laxika.magicalvibes.cards.s.ShorelineLooter;
import com.github.laxika.magicalvibes.cards.t.ThreeTreeMascot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ValleyFloodcaller.class, BloomingBlast.class, DruidOfTheSpade.class, FinchFormation.class,
        FountainportBell.class, KitsaOtterballElite.class, MindSpiral.class, PondProphet.class,
        ShorelineLooter.class, ThreeTreeMascot.class})
class ValleyFloodcallerTest extends BaseCardTest {

    @Test
    void grantsFlashToNoncreatureSpells() {
        harness.addToBattlefield(player1, new ValleyFloodcaller());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        castMindSpiral();

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Mind Spiral"));
    }

    @Test
    void doesNotGrantFlashToCreatureSpells() {
        harness.addToBattlefield(player1, new ValleyFloodcaller());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DruidOfTheSpade()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void buffsAndUntapsControlledBirdsFrogsOttersAndRatsOnly() {
        Permanent floodcaller = addCreatureReady(player1, new ValleyFloodcaller());
        Permanent bird = addCreatureReady(player1, new FinchFormation());
        Permanent frog = addCreatureReady(player1, new PondProphet());
        Permanent otter = addCreatureReady(player1, new KitsaOtterballElite());
        Permanent rat = addCreatureReady(player1, new ShorelineLooter());
        Permanent druid = addCreatureReady(player1, new DruidOfTheSpade());
        Permanent opponentOtter = addCreatureReady(player2, new KitsaOtterballElite());

        List.of(floodcaller, bird, frog, otter, rat, druid, opponentOtter).forEach(Permanent::tap);
        int druidPower = gqs.getEffectivePower(gd, druid);
        int druidToughness = gqs.getEffectiveToughness(gd, druid);
        int opponentOtterPower = gqs.getEffectivePower(gd, opponentOtter);
        int opponentOtterToughness = gqs.getEffectiveToughness(gd, opponentOtter);

        castMindSpiral();
        resolveAllTriggers();

        assertThat(List.of(floodcaller, bird, frog, rat))
                .allSatisfy(permanent -> {
                    assertThat(permanent.isTapped()).isFalse();
                    assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(
                            permanent.getCard().getPower() + 1);
                    assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(
                            permanent.getCard().getToughness() + 1);
                });
        assertThat(otter.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, otter)).isEqualTo(otter.getCard().getPower() + 2);
        assertThat(gqs.getEffectiveToughness(gd, otter)).isEqualTo(otter.getCard().getToughness() + 2);
        assertThat(druid.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, druid)).isEqualTo(druidPower);
        assertThat(gqs.getEffectiveToughness(gd, druid)).isEqualTo(druidToughness);
        assertThat(opponentOtter.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentOtter)).isEqualTo(opponentOtterPower);
        assertThat(gqs.getEffectiveToughness(gd, opponentOtter)).isEqualTo(opponentOtterToughness);
    }

    @Test
    void hasFlashWithoutAnotherFloodcallerAndDoesNotTriggerItself() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ValleyFloodcaller()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Valley Floodcaller");
    }

    @Test
    void doesNotGrantFlashToOpponentsSorceries() {
        harness.addToBattlefield(player1, new ValleyFloodcaller());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new MindSpiral()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castSorceryWithGift(player2, 0, List.of(player1.getId()), false))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void creatureSpellDoesNotBoostOrUntap() {
        Permanent floodcaller = addCreatureReady(player1, new ValleyFloodcaller());
        floodcaller.tap();
        harness.setHand(player1, List.of(new DruidOfTheSpade()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(floodcaller.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, floodcaller)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, floodcaller)).isEqualTo(2);
    }

    @Test
    void triggerResolvesBeforeSpellAndIncludesCreaturesPresentAtResolution() {
        Permanent floodcaller = addCreatureReady(player1, new ValleyFloodcaller());
        floodcaller.tap();
        castMindSpiral();
        Permanent frog = addCreatureReady(player1, new PondProphet());
        frog.tap();

        assertThat(floodcaller.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, floodcaller)).isEqualTo(2);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(floodcaller.isTapped()).isFalse();
        assertThat(frog.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, frog)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, frog)).isEqualTo(2);
        Permanent laterRat = addCreatureReady(player1, new ShorelineLooter());
        assertThat(gqs.getEffectivePower(gd, laterRat)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, laterRat)).isEqualTo(1);
    }

    @Test
    void boostsStackAndExpireAtEndOfTurn() {
        Permanent floodcaller = addCreatureReady(player1, new ValleyFloodcaller());
        castMindSpiral();
        resolveAllTriggers();
        floodcaller.tap();
        castMindSpiral();
        resolveAllTriggers();

        assertThat(floodcaller.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, floodcaller)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, floodcaller)).isEqualTo(4);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, floodcaller)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, floodcaller)).isEqualTo(2);
    }

    @Test
    void multipleFloodcallersEachTrigger() {
        Permanent first = addCreatureReady(player1, new ValleyFloodcaller());
        Permanent second = addCreatureReady(player1, new ValleyFloodcaller());
        first.tap();
        second.tap();

        castMindSpiral();

        assertThat(gd.stack).hasSize(3);
        resolveAllTriggers();
        assertThat(List.of(first, second)).allSatisfy(permanent -> {
            assertThat(permanent.isTapped()).isFalse();
            assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(4);
        });
    }

    @Test
    void opponentsSpellDoesNotTriggerAndTriggerSurvivesSourceRemoval() {
        Permanent floodcaller = addCreatureReady(player1, new ValleyFloodcaller());
        Permanent rat = addCreatureReady(player1, new ShorelineLooter());
        rat.tap();
        castMindSpiral();
        harness.setHand(player2, List.of(new BloomingBlast()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstantWithGift(player2, 0, floodcaller.getId(), false);

        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Valley Floodcaller");
        assertThat(rat.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(rat.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, rat)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rat)).isEqualTo(2);
    }

    @Test
    void grantsFlashToNoncreatureArtifacts() {
        harness.addToBattlefield(player1, new ValleyFloodcaller());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new FountainportBell()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    void artifactCreaturesDoNotReceiveFlashPermission() {
        harness.addToBattlefield(player1, new ValleyFloodcaller());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ThreeTreeMascot()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void changelingReceivesOnlyOneBoostDespiteHavingAllFourTypes() {
        harness.addToBattlefield(player1, new ValleyFloodcaller());
        Permanent mascot = addCreatureReady(player1, new ThreeTreeMascot());
        mascot.tap();

        castMindSpiral();
        resolveAllTriggers();

        assertThat(mascot.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, mascot)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mascot)).isEqualTo(2);
    }

    private void castMindSpiral() {
        harness.setHand(player1, List.of(new MindSpiral()));
        harness.setLibrary(player2, List.of(new ShorelineLooter(), new ShorelineLooter(),
                new ShorelineLooter(), new ShorelineLooter(), new ShorelineLooter(), new ShorelineLooter()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castSorceryWithGift(player1, 0, List.of(player2.getId()), false);
    }
}