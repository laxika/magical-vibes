package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GideonBlackblade;
import com.github.laxika.magicalvibes.cards.h.HeartbeatOfSpring;
import com.github.laxika.magicalvibes.cards.n.NeurokTransmuter;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        ConvertToSlime.class,
        FountainOfYouth.class,
        Forest.class,
        GrizzlyBears.class,
        GideonBlackblade.class,
        HeartbeatOfSpring.class,
        NeurokTransmuter.class,
        Ornithopter.class,
        Shock.class
})
class ConvertToSlimeTest extends BaseCardTest {

    @Test
    void destroysEachTypeAndCreatesOozeWithTotalManaValueWhenDeliriumIsActive() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new HeartbeatOfSpring());
        harness.setGraveyard(player1, List.of(new Forest(), new Shock()));
        harness.setHand(player1, List.of(new ConvertToSlime()));
        addManaForConvertToSlime();

        harness.castAndResolveSorcery(player1, 0, List.of(artifact.getId(), creature.getId(), enchantment.getId()));

        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Heartbeat of Spring");
        Permanent ooze = findPermanent(player1, "Ooze");
        assertThat(ooze.getCard().getPower()).isEqualTo(5);
        assertThat(ooze.getCard().getToughness()).isEqualTo(5);
    }

    @Test
    void doesNotCreateOozeWithoutDelirium() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new ConvertToSlime()));
        addManaForConvertToSlime();

        harness.castAndResolveSorcery(player1, 0, List.of(creature.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Ooze")).isEmpty();
    }

    @Test
    void rejectsMoreThanOneTargetOfTheSameType() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ConvertToSlime()));
        addManaForConvertToSlime();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(firstCreature.getId(), secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("declared target groups");
    }

    @Test
    void canResolveWithoutChoosingAnyTargets() {
        harness.setHand(player1, List.of(new ConvertToSlime()));
        addManaForConvertToSlime();

        harness.castAndResolveSorcery(player1, 0, List.of());

        harness.assertInGraveyard(player1, "Convert to Slime");
        assertThat(findPermanents(player1, "Ooze")).isEmpty();
    }

    @Test
    void canChooseTheSameArtifactCreatureForBothTargetRoles() {
        harness.addToBattlefield(player1, new NeurokTransmuter());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new HeartbeatOfSpring());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 0, null, bear.getId());
        harness.passBothPriorities();
        harness.setGraveyard(player1, List.of(new Forest(), new Shock(), new FountainOfYouth(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new ConvertToSlime()));
        addManaForConvertToSlime();

        harness.castAndResolveSorcery(player1, 0, List.of(bear.getId(), bear.getId(), enchantment.getId()));

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Heartbeat of Spring");
        Permanent ooze = findPermanent(player1, "Ooze");
        assertThat(ooze.getCard().getPower()).isEqualTo(5);
        assertThat(ooze.getCard().getToughness()).isEqualTo(5);
    }

    @Test
    void artifactCreatureCanFillArtifactRoleAlongsideAnotherCreature() {
        Permanent thopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new HeartbeatOfSpring());
        harness.setGraveyard(player1, List.of(new Forest(), new Shock()));
        harness.setHand(player1, List.of(new ConvertToSlime()));
        addManaForConvertToSlime();

        harness.castAndResolveSorcery(player1, 0, List.of(thopter.getId(), bear.getId(), enchantment.getId()));

        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Heartbeat of Spring");
        Permanent ooze = findPermanent(player1, "Ooze");
        assertThat(ooze.getCard().getPower()).isEqualTo(5);
        assertThat(ooze.getCard().getToughness()).isEqualTo(5);
    }

    @Test
    void animatedPlaneswalkerCannotFillAnExtraTargetRole() {
        Permanent gideon = harness.addToBattlefieldAndReturn(player1, new GideonBlackblade());
        gideon.setCounterCount(CounterType.LOYALTY, 4);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new HeartbeatOfSpring());
        harness.setHand(player1, List.of(new ConvertToSlime()));
        addManaForConvertToSlime();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(gideon.getId(), bear.getId(), enchantment.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void indestructibleTargetDoesNotContributeToOozeSize() {
        Permanent gideon = harness.addToBattlefieldAndReturn(player1, new GideonBlackblade());
        gideon.setCounterCount(CounterType.LOYALTY, 4);
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new HeartbeatOfSpring());
        harness.setGraveyard(player1, List.of(new Forest(), new Shock(), new FountainOfYouth(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new ConvertToSlime()));
        addManaForConvertToSlime();

        harness.castAndResolveSorcery(player1, 0, List.of(gideon.getId(), enchantment.getId()));

        harness.assertOnBattlefield(player1, "Gideon Blackblade");
        harness.assertInGraveyard(player2, "Heartbeat of Spring");
        Permanent ooze = findPermanent(player1, "Ooze");
        assertThat(ooze.getCard().getPower()).isEqualTo(3);
        assertThat(ooze.getCard().getToughness()).isEqualTo(3);
    }

    @Test
    void remainingLegalTargetResolvesAndAloneContributesToOozeSize() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new HeartbeatOfSpring());
        harness.setGraveyard(player1, List.of(new Forest(), new Shock(), new FountainOfYouth(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new ConvertToSlime()));
        harness.setHand(player2, List.of(new Shock()));
        addManaForConvertToSlime();
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, List.of(bear.getId(), enchantment.getId()));
        harness.castAndResolveInstant(player2, 0, bear.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Heartbeat of Spring");
        Permanent ooze = findPermanent(player1, "Ooze");
        assertThat(ooze.getCard().getPower()).isEqualTo(3);
        assertThat(ooze.getCard().getToughness()).isEqualTo(3);
    }

    @Test
    void allTargetsBecomingIllegalPreventsTokenCreationDespiteDelirium() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Forest(), new Shock(), new FountainOfYouth(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new ConvertToSlime()));
        harness.setHand(player2, List.of(new Shock()));
        addManaForConvertToSlime();
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, List.of(bear.getId()));
        harness.castAndResolveInstant(player2, 0, bear.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Convert to Slime");
        assertThat(findPermanents(player1, "Ooze")).isEmpty();
    }

    @Test
    void artifactTargetThatLosesArtifactTypeIsNotDestroyedAsACreatureInstead() {
        Permanent thopter = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new NeurokTransmuter());
        harness.setHand(player1, List.of(new ConvertToSlime()));
        addManaForConvertToSlime();
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, List.of(thopter.getId(), bear.getId()));
        harness.activateAbility(player2, 2, 1, null, thopter.getId());
        harness.passBothPriorities();
        assertThat(gqs.isArtifact(gd, thopter)).isFalse();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void resolvingSpellDoesNotCountItsOwnSorceryTypeForDelirium() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Forest(), new Shock(), new FountainOfYouth()));
        harness.setHand(player1, List.of(new ConvertToSlime()));
        addManaForConvertToSlime();

        harness.castAndResolveSorcery(player1, 0, List.of(bear.getId()));

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Convert to Slime");
        assertThat(findPermanents(player1, "Ooze")).isEmpty();
    }

    private void addManaForConvertToSlime() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
