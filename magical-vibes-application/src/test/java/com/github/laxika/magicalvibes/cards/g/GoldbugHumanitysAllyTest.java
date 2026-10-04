package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.e.EssenceScatter;
import com.github.laxika.magicalvibes.cards.m.MishrasBauble;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoldbugHumanitysAlly.class, GoldbugScrappyScout.class, EliteVanguard.class,
        EssenceScatter.class, GrizzlyBears.class, MishrasBauble.class, Mountain.class,
        TurnToFrog.class, Unsummon.class})
class GoldbugHumanitysAllyTest extends BaseCardTest {

    @Test
    void moreThanMeetsTheEyeCastsGoldbugConvertedWithLivingMetal() {
        Permanent goldbug = castGoldbugConverted();

        assertThat(goldbug.isTransformed()).isTrue();
        assertThat(goldbug.getCard()).isInstanceOf(GoldbugScrappyScout.class);
        assertThat(gqs.isCreature(gd, goldbug)).isTrue();
    }

    @Test
    void convertsAfterTheSecondSpellEachTurn() {
        Permanent goldbug = harness.addToBattlefieldAndReturn(player1, new GoldbugHumanitysAlly());
        harness.setHand(player1, List.of(new MishrasBauble(), new MishrasBauble()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(goldbug.isTransformed()).isTrue();
        assertThat(goldbug.getCard()).isInstanceOf(GoldbugScrappyScout.class);
    }

    @Test
    void preventsCombatDamageToAttackingHumansButNotOtherAttackers() {
        Permanent goldbug = addCreatureReady(player1, new GoldbugHumanitysAlly());
        Permanent human = addCreatureReady(player1, new EliteVanguard());
        Permanent humanBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent goldbugBlocker = addCreatureReady(player2, new GrizzlyBears());

        human.setAttacking(true);
        goldbug.setAttacking(true);
        humanBlocker.setBlocking(true);
        humanBlocker.addBlockingTarget(1);
        goldbugBlocker.setBlocking(true);
        goldbugBlocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(human.getMarkedDamage()).isZero();
        assertThat(goldbug.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void humanSpellsYouControlCannotBeCountered() {
        Permanent goldbug = castGoldbugConverted();
        EliteVanguard human = new EliteVanguard();
        EssenceScatter scatter = new EssenceScatter();
        harness.setHand(player1, List.of(human));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);

        harness.ensurePriority(player2);
        harness.setHand(player2, List.of(scatter));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, human.getId());
        resolveAllTriggers();

        assertThat(goldbug.isTransformed()).isTrue();
        harness.assertOnBattlefield(player1, "Elite Vanguard");
        harness.assertInGraveyard(player2, "Essence Scatter");
    }

    @Test
    void nonHumanSpellsCanStillBeCountered() {
        castGoldbugConverted();
        GrizzlyBears bears = new GrizzlyBears();
        EssenceScatter scatter = new EssenceScatter();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        harness.ensurePriority(player2);
        harness.setHand(player2, List.of(scatter));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, bears.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Essence Scatter");
    }

    @Test
    void attackingWithGoldbugAndAHumanDrawsAndConverts() {
        Permanent goldbug = castGoldbugConverted();
        goldbug.setSummoningSick(false);
        addCreatureReady(player1, new EliteVanguard());
        harness.setLibrary(player1, List.of(new Mountain()));

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(goldbug.isTransformed()).isFalse();
        assertThat(goldbug.getCard()).isInstanceOf(GoldbugHumanitysAlly.class);
    }

    @Test
    void attackingWithOnlyAHumanDoesNotTriggerGoldbug() {
        Permanent goldbug = castGoldbugConverted();
        addCreatureReady(player1, new EliteVanguard());
        harness.setLibrary(player1, List.of(new Mountain()));

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(goldbug.isTransformed()).isTrue();
    }

    @Test
    void livingMetalDoesNotMakeGoldbugACreatureDuringOpponentsTurn() {
        Permanent goldbug = castGoldbugConverted();

        harness.forceActivePlayer(player2);

        assertThat(gqs.isCreature(gd, goldbug)).isFalse();
        harness.forceActivePlayer(player1);
        assertThat(gqs.isCreature(gd, goldbug)).isTrue();
    }

    @Test
    void firstSpellDoesNotConvertGoldbug() {
        Permanent goldbug = harness.addToBattlefieldAndReturn(player1, new GoldbugHumanitysAlly());
        harness.setHand(player1, List.of(new MishrasBauble()));

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(goldbug.isTransformed()).isFalse();
    }

    @Test
    void secondSpellDuringOpponentsTurnAlsoConvertsGoldbug() {
        Permanent goldbug = harness.addToBattlefieldAndReturn(player1, new GoldbugHumanitysAlly());
        Permanent firstTarget = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondTarget = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new Unsummon(), new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, firstTarget.getId());
        resolveAllTriggers();
        assertThat(goldbug.isTransformed()).isFalse();
        harness.castInstant(player1, 0, secondTarget.getId());
        resolveAllTriggers();

        assertThat(goldbug.isTransformed()).isTrue();
        assertThat(gqs.isCreature(gd, goldbug)).isFalse();
    }

    @Test
    void opponentsHumanSpellCanBeCountered() {
        castGoldbugConverted();
        harness.forceActivePlayer(player2);
        EliteVanguard human = new EliteVanguard();
        harness.setHand(player2, List.of(human));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castCreature(player2, 0);
        harness.setHand(player1, List.of(new EssenceScatter()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, human.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Elite Vanguard");
        harness.assertNotOnBattlefield(player2, "Elite Vanguard");
    }

    @Test
    void attackingWithGoldbugAloneDoesNotDrawOrConvert() {
        Permanent goldbug = castGoldbugConverted();
        goldbug.setSummoningSick(false);
        harness.setLibrary(player1, List.of(new Mountain()));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(goldbug.isTransformed()).isTrue();
    }

    @Test
    void attackingWithMultipleHumansDrawsOnlyOneCard() {
        Permanent goldbug = castGoldbugConverted();
        goldbug.setSummoningSick(false);
        addCreatureReady(player1, new EliteVanguard());
        addCreatureReady(player1, new EliteVanguard());
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain()));

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(goldbug.isTransformed()).isFalse();
    }

    @Test
    void removingTheHumanInResponseDoesNotStopDrawingAndConverting() {
        Permanent goldbug = castGoldbugConverted();
        goldbug.setSummoningSick(false);
        Permanent human = addCreatureReady(player1, new EliteVanguard());
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0, 1)));
        harness.castInstant(player2, 0, human.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Elite Vanguard");
        harness.assertInHand(player1, "Mountain");
        assertThat(goldbug.isTransformed()).isFalse();
    }

    @Test
    void losingAllAbilitiesStopsProtectingAttackingHumans() {
        Permanent goldbug = addCreatureReady(player1, new GoldbugHumanitysAlly());
        Permanent human = addCreatureReady(player1, new EliteVanguard());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new TurnToFrog()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, goldbug.getId());
        resolveAllTriggers();

        human.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);
        resolveCombat();

        harness.assertInGraveyard(player1, "Elite Vanguard");
        harness.assertNotOnBattlefield(player1, "Elite Vanguard");
    }

    private Permanent castGoldbugConverted() {
        harness.setHand(player1, List.of(new GoldbugHumanitysAlly()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Goldbug, Scrappy Scout");
    }
}
