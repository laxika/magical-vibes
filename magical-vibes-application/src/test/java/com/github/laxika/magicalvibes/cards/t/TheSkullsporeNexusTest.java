package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MasumaroFirstToLive;
import com.github.laxika.magicalvibes.cards.p.PlanarCleansing;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheSkullsporeNexus.class, GrizzlyBears.class, MasumaroFirstToLive.class,
        WrathOfGod.class, PlanarCleansing.class})
class TheSkullsporeNexusTest extends BaseCardTest {

    @Test
    @DisplayName("Cost reduction uses the greatest controlled creature power")
    void costReductionUsesGreatestPower() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TheSkullsporeNexus()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Playability evaluates the reduction after the spell leaves its controller's hand")
    void playabilityEvaluatesReductionAfterSpellLeavesHand() {
        harness.addToBattlefield(player1, new MasumaroFirstToLive());
        harness.setHand(player1, List.of(new TheSkullsporeNexus(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.ensurePriority(player1);

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).doesNotContain(0);

        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).contains(0);
    }

    @Test
    @DisplayName("Simultaneous nontoken deaths create one Fungus Dinosaur with their total power")
    void simultaneousDeathsCreateOneTokenWithTotalPower() {
        harness.addToBattlefield(player1, new TheSkullsporeNexus());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        destroyCreaturesWithWrath();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Fungus Dinosaur");
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.FUNGUS, CardSubtype.DINOSAUR);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
    }

    @Test
    @DisplayName("Activated ability doubles a target creature's power")
    void activatedAbilityDoublesTargetCreaturePower() {
        harness.addToBattlefield(player1, new TheSkullsporeNexus());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void opposingCreaturesDoNotReduceCastingCost() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TheSkullsporeNexus()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void reductionCannotRemoveColoredManaRequirements() {
        harness.addToBattlefield(player1, new MasumaroFirstToLive());
        harness.setHand(player1, List.of(new TheSkullsporeNexus(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.ensurePriority(player1);

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).doesNotContain(0);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void opposingDeathsDoNotTriggerTokenCreation() {
        harness.addToBattlefield(player1, new TheSkullsporeNexus());
        harness.addToBattlefield(player2, new GrizzlyBears());

        destroyCreaturesWithWrath();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Fungus Dinosaur")).isEmpty();
    }

    @Test
    void tokenDeathsDoNotTriggerTokenCreation() {
        harness.addToBattlefield(player1, new TheSkullsporeNexus());
        harness.addToBattlefield(player1, new GrizzlyBears());
        destroyCreaturesWithWrath();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Fungus Dinosaur")).hasSize(1);

        destroyCreaturesWithWrath();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Fungus Dinosaur")).isEmpty();
    }

    @Test
    void simultaneousDeathsExcludeTokensAndOpposingCreaturesFromTotalPower() {
        harness.addToBattlefield(player1, new TheSkullsporeNexus());
        harness.addToBattlefield(player1, new GrizzlyBears());
        destroyCreaturesWithWrath();
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        destroyCreaturesWithWrath();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        List<Permanent> tokens = findPermanents(player1, "Fungus Dinosaur");
        assertThat(tokens).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, tokens.getFirst())).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, tokens.getFirst())).isEqualTo(2);
    }

    @Test
    void deathTriggerUsesLastKnownModifiedPower() {
        harness.addToBattlefield(player1, new TheSkullsporeNexus());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        destroyCreaturesWithWrath();
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Fungus Dinosaur");
        assertThat(tokens).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, tokens.getFirst())).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, tokens.getFirst())).isEqualTo(4);
    }

    @Test
    void doublingCanTargetOpposingCreatureAndExpiresAtEndOfTurn() {
        Permanent nexus = harness.addToBattlefieldAndReturn(player1, new TheSkullsporeNexus());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, bears.getId());
        assertThat(nexus.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void simultaneousDestructionOfNexusAndCreaturesStillCreatesToken() {
        harness.addToBattlefield(player1, new TheSkullsporeNexus());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new PlanarCleansing()));
        harness.addMana(player2, ManaColor.WHITE, 6);
        harness.forceActivePlayer(player2);

        harness.castAndResolveSorcery(player2, 0, 0);

        harness.assertNotOnBattlefield(player1, "The Skullspore Nexus");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        List<Permanent> tokens = findPermanents(player1, "Fungus Dinosaur");
        assertThat(tokens).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, tokens.getFirst())).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, tokens.getFirst())).isEqualTo(4);
    }

    private void destroyCreaturesWithWrath() {
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);

        harness.castAndResolveSorcery(player2, 0, 0);
    }
}
