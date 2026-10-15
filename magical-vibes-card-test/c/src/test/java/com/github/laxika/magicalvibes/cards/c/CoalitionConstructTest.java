package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.UrborgElf;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoalitionConstruct.class, GrizzlyBears.class, UrborgElf.class, Unsummon.class})
class CoalitionConstructTest extends BaseCardTest {

    @Test
    void perpetuallyBoostsMatchingCreaturesAndCreatureCardsInHand() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new UrborgElf());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        GrizzlyBears handBear = new GrizzlyBears();

        harness.setHand(player1, List.of(new CoalitionConstruct(), handBear));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.BEAR.name());
        harness.passBothPriorities();

        Permanent construct = findPermanent(player1, "Coalition Construct");
        assertThat(construct.getChosenSubtype()).isEqualTo(CardSubtype.BEAR);
        assertThat(gqs.hasEffectiveSubtype(gd, construct, CardSubtype.BEAR)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, construct, CardSubtype.CONSTRUCT)).isTrue();
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBear)).isEqualTo(2);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent enteredBear = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == handBear)
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, enteredBear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enteredBear)).isEqualTo(3);
    }

    @Test
    void triggerStillBoostsCreaturesWhenConstructLeavesBeforeResolution() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CoalitionConstruct(), new Unsummon(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.BEAR.name());

        Permanent construct = findPermanent(player1, "Coalition Construct");
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, construct.getId());
        harness.assertNotOnBattlefield(player1, "Coalition Construct");
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent handBear = findPermanents(player1, "Grizzly Bears").get(1);
        assertThat(gqs.getEffectivePower(gd, handBear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, handBear)).isEqualTo(3);
    }

    @Test
    void perpetualBoostSurvivesBounceAndDoesNotApplyToLaterCreatures() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CoalitionConstruct(), new Unsummon()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.BEAR.name());
        resolveAllTriggers();

        Permanent laterBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, laterBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, laterBear)).isEqualTo(2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, bear.getId());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent returnedBear = findPermanents(player1, "Grizzly Bears").get(1);
        assertThat(gqs.getEffectivePower(gd, returnedBear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, returnedBear)).isEqualTo(3);
    }

    @Test
    void boostsOtherConstructsAndPreservesHandBoostWhenCastWithoutBoostingItsSource() {
        harness.setHand(player1, List.of(new CoalitionConstruct(), new CoalitionConstruct()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.CONSTRUCT.name());
        resolveAllTriggers();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.CONSTRUCT.name());
        resolveAllTriggers();

        List<Permanent> constructs = findPermanents(player1, "Coalition Construct");
        assertThat(gqs.getEffectivePower(gd, constructs.get(0))).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, constructs.get(0))).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, constructs.get(1))).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, constructs.get(1))).isEqualTo(3);
    }

    @Test
    void boostsFromMultipleEntriesAccumulate() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CoalitionConstruct(), new CoalitionConstruct(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.BEAR.name());
        resolveAllTriggers();
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.BEAR.name());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent handBear = findPermanents(player1, "Grizzly Bears").get(1);
        assertThat(gqs.getEffectivePower(gd, handBear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, handBear)).isEqualTo(4);
    }
}
