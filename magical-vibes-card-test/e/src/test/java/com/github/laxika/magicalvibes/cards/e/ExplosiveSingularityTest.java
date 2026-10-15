package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.t.TezzeretBetrayerOfFlesh;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExplosiveSingularity.class, GrizzlyBears.class, Spellbook.class, TezzeretBetrayerOfFlesh.class})
class ExplosiveSingularityTest extends BaseCardTest {

    @Test
    void tapsCreaturesToReduceTheGenericCost() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ExplosiveSingularity()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castSorceryWithSacrifices(player1, 0, player2.getId(),
                List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
    }

    @Test
    void canBeCastWithoutTappingCreatures() {
        harness.setHand(player1, List.of(new ExplosiveSingularity()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castSorceryWithSacrifices(player1, 0, player2.getId(), List.of());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
    }

    @Test
    void cannotTapNonCreatureToReduceTheCost() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setHand(player1, List.of(new ExplosiveSingularity()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifices(
                player1, 0, player2.getId(), List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(artifact.isTapped()).isFalse();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void canTapSummoningSickCreatureAndTargetIt() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setSummoningSick(true);
        harness.setHand(player1, List.of(new ExplosiveSingularity()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castSorceryTappingPermanents(player1, 0, creature.getId(), List.of(creature.getId()));
        assertThat(creature.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotTapAlreadyTappedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.tap();
        harness.setHand(player1, List.of(new ExplosiveSingularity()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        assertThatThrownBy(() -> harness.castSorceryTappingPermanents(
                player1, 0, player2.getId(), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Explosive Singularity");
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotTapOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ExplosiveSingularity()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        assertThatThrownBy(() -> harness.castSorceryTappingPermanents(
                player1, 0, player2.getId(), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
        harness.assertInHand(player1, "Explosive Singularity");
    }

    @Test
    void cannotCountSameCreatureTwice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ExplosiveSingularity()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        assertThatThrownBy(() -> harness.castSorceryTappingPermanents(
                player1, 0, player2.getId(), List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
        harness.assertInHand(player1, "Explosive Singularity");
    }

    @Test
    void canTapMoreCreaturesThanGenericCost() {
        List<Permanent> creatures = new ArrayList<>();
        List<UUID> ids = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            creatures.add(creature);
            ids.add(creature.getId());
        }
        harness.setHand(player1, List.of(new ExplosiveSingularity()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorceryTappingPermanents(player1, 0, player2.getId(), ids);

        assertThat(creatures).allSatisfy(creature -> assertThat(creature.isTapped()).isTrue());
        harness.passBothPriorities();
        harness.assertLife(player2, 10);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(9);
    }

    @Test
    void tappingCreaturesCannotPayRedManaRequirement() {
        List<UUID> ids = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            ids.add(harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId());
        }
        harness.setHand(player1, List.of(new ExplosiveSingularity()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorceryTappingPermanents(player1, 0, player2.getId(), ids))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allSatisfy(creature -> assertThat(creature.isTapped()).isFalse());
        harness.assertInHand(player1, "Explosive Singularity");
    }

    @Test
    void canTargetItsController() {
        harness.setHand(player1, List.of(new ExplosiveSingularity()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 20);
    }

    @Test
    void dealsTenDamageToPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new TezzeretBetrayerOfFlesh());
        planeswalker.setCounterCount(CounterType.LOYALTY, 12);
        harness.setHand(player1, List.of(new ExplosiveSingularity()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castAndResolveSorcery(player1, 0, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Tezzeret, Betrayer of Flesh");
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotTargetNoncreatureArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        harness.setHand(player1, List.of(new ExplosiveSingularity()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Explosive Singularity");
        harness.assertOnBattlefield(player2, "Spellbook");
    }
}
