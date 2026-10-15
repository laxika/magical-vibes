package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CloudPirates;
import com.github.laxika.magicalvibes.cards.d.Disentomb;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.ManorSkeleton;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkeletonCrew.class, CloudPirates.class, ManorSkeleton.class, GrizzlyBears.class, Disentomb.class})
class SkeletonCrewTest extends BaseCardTest {

    @Test
    void boostsOtherSkeletonsAndPiratesYouControl() {
        Permanent crew = addCreatureReady(player1, new SkeletonCrew());
        Permanent skeleton = addCreatureReady(player1, new ManorSkeleton());
        Permanent pirate = addCreatureReady(player1, new CloudPirates());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, crew)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, skeleton)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, skeleton)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, pirate)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, pirate)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    void createsOneSkeletonPirateWhenCreatureCardLeavesGraveyard() {
        harness.addToBattlefield(player1, new SkeletonCrew());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Skeleton Pirate");
        assertThat(tokens).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, tokens.getFirst())).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, tokens.getFirst())).isEqualTo(3);
    }

    @Test
    void returnsItselfFromGraveyardTapped() {
        SkeletonCrew crew = new SkeletonCrew();
        harness.setGraveyard(player1, List.of(crew));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Skeleton Crew");
        assertThat(returned.isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Skeleton Crew");
    }

    @Test
    void returningItselfDoesNotTriggerItsOwnTokenAbility() {
        harness.setGraveyard(player1, List.of(new SkeletonCrew()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Skeleton Crew");
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Skeleton Pirate")).isEmpty();
    }

    @Test
    void boostsAnotherCrewOnlyOnceDespiteBothMatchingSubtypes() {
        Permanent first = addCreatureReady(player1, new SkeletonCrew());
        Permanent second = addCreatureReady(player1, new SkeletonCrew());
        Permanent opponent = addCreatureReady(player2, new SkeletonCrew());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(3);
    }

    @Test
    void onlyCrewAlreadyOnBattlefieldTriggersWhenAnotherCrewReturns() {
        harness.addToBattlefield(player1, new SkeletonCrew());
        harness.setGraveyard(player1, List.of(new SkeletonCrew()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        List<Permanent> tokens = findPermanents(player1, "Skeleton Pirate");
        assertThat(tokens).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, tokens.getFirst())).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, tokens.getFirst())).isEqualTo(4);
    }

    @Test
    void doesNotTriggerForAnOpponentsGraveyard() {
        harness.addToBattlefield(player1, new SkeletonCrew());
        SkeletonCrew card = new SkeletonCrew();
        harness.setGraveyard(player2, List.of(card));
        harness.setHand(player2, List.of(new Disentomb()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);

        harness.castAndResolveSorcery(player2, 0, card.getId());

        harness.assertInHand(player2, "Skeleton Crew");
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Skeleton Pirate")).isEmpty();
    }

    @Test
    void abilityDoesNotTriggerWhileCrewIsInGraveyard() {
        SkeletonCrew crew = new SkeletonCrew();
        SkeletonCrew other = new SkeletonCrew();
        harness.setGraveyard(player1, List.of(crew, other));
        harness.setHand(player1, List.of(new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, other.getId());

        harness.assertInHand(player1, "Skeleton Crew");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(crew);
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Skeleton Pirate")).isEmpty();
    }
}
