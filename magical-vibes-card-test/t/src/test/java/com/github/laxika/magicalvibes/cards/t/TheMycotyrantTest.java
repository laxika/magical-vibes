package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AnotherChance;
import com.github.laxika.magicalvibes.cards.a.ArtificialEvolution;
import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.b.BroodrageMycoid;
import com.github.laxika.magicalvibes.cards.d.DeathcapMarionette;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YavimayaSapherd;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheMycotyrant.class, BroodrageMycoid.class, GrizzlyBears.class, YavimayaSapherd.class,
        ZuranOrb.class, Forest.class, AnotherChance.class, ArtificialEvolution.class,
        Bitterblossom.class, DeathcapMarionette.class})
class TheMycotyrantTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness count controlled Fungi and Saprolings")
    void powerAndToughnessCountFungiAndSaprolings() {
        Permanent mycotyrant = harness.addToBattlefieldAndReturn(player1, new TheMycotyrant());
        harness.addToBattlefield(player1, new BroodrageMycoid());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new YavimayaSapherd()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, mycotyrant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mycotyrant)).isEqualTo(4);
    }

    @Test
    @DisplayName("Creates one nonblocking Fungus token for each descent at your end step")
    void createsTokensForEachDescent() {
        harness.addToBattlefield(player1, new TheMycotyrant());
        harness.addToBattlefield(player1, new ZuranOrb());
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, firstLand.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Fungus").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> assertThat(bls.canBlock(gd, token)).isFalse());
    }

    @Test
    void noncreatureFungusDoesNotIncreasePowerOrToughness() {
        Permanent mycotyrant = harness.addToBattlefieldAndReturn(player1, new TheMycotyrant());
        Permanent blossom = harness.addToBattlefieldAndReturn(player1, new Bitterblossom());
        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, blossom.getId());
        harness.handleListChoice(player1, "FAERIE");
        harness.handleListChoice(player1, "FUNGUS");

        assertThat(gqs.getEffectivePower(gd, mycotyrant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mycotyrant)).isEqualTo(1);
    }

    @Test
    void creatureWithBothTypesCountsOnceAndOpposingFungiDoNotCount() {
        Permanent mycotyrant = harness.addToBattlefieldAndReturn(player1, new TheMycotyrant());
        harness.addToBattlefield(player1, new DeathcapMarionette());
        harness.addToBattlefield(player2, new BroodrageMycoid());
        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, mycotyrant.getId());
        harness.handleListChoice(player1, "ELDER");
        harness.handleListChoice(player1, "SAPROLING");

        assertThat(gqs.getEffectivePower(gd, mycotyrant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mycotyrant)).isEqualTo(2);
    }

    @Test
    void millingCountsPermanentCardsButNotInstants() {
        Permanent mycotyrant = harness.addToBattlefieldAndReturn(player1, new TheMycotyrant());
        harness.setLibrary(player1, List.of(new Forest(), new AnotherChance()));
        harness.setHand(player1, List.of(new DeathcapMarionette()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Fungus")).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, mycotyrant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mycotyrant)).isEqualTo(3);
    }

    @Test
    void descentAfterEndStepTriggerIsIncludedAtResolution() {
        harness.addToBattlefield(player1, new TheMycotyrant());
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new Forest());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Fungus")).hasSize(1);
    }

    @Test
    void characteristicAbilityWorksInHandWithoutCountingItself() {
        TheMycotyrant mycotyrant = new TheMycotyrant();
        harness.setHand(player1, List.of(mycotyrant));
        harness.addToBattlefield(player1, new DeathcapMarionette());
        harness.addToBattlefield(player2, new BroodrageMycoid());

        assertThat(gqs.getEffectiveCardPower(gd, mycotyrant)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, mycotyrant)).isEqualTo(1);

        harness.addToBattlefield(player1, new DeathcapMarionette());

        assertThat(gqs.getEffectiveCardPower(gd, mycotyrant)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, mycotyrant)).isEqualTo(2);
    }

    @Test
    void previousTurnsDescentsDoNotCreateMoreTokens() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.addToBattlefield(player1, new TheMycotyrant());
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new Forest());
        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Fungus")).hasSize(1);

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Fungus")).hasSize(1);
    }

    @Test
    void noDescentsCreatesNoTokens() {
        harness.addToBattlefield(player1, new TheMycotyrant());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Fungus")).isEmpty();
    }

    @Test
    void doesNotTriggerAtOpponentsEndStep() {
        harness.addToBattlefield(player1, new TheMycotyrant());
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new Forest());
        harness.forceActivePlayer(player2);
        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Fungus")).isEmpty();
    }
}
