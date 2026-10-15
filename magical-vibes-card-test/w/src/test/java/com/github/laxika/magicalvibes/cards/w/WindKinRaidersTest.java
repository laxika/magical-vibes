package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DruidOfTheCowl;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WindKinRaiders.class, Ornithopter.class, DruidOfTheCowl.class})
class WindKinRaidersTest extends BaseCardTest {

    @Test
    @DisplayName("Improvise taps an artifact to pay generic mana")
    void improviseTapsArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.setHand(player1, List.of(new WindKinRaiders()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).contains(0);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(artifact.getId()));

        assertThat(artifact.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Wind-Kin Raiders")).isNotNull();
    }

    @Test
    @DisplayName("Improvise cannot tap a nonartifact permanent")
    void improviseRejectsNonartifact() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DruidOfTheCowl());
        harness.setHand(player1, List.of(new WindKinRaiders()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> gs.playCard(
                gd, player1, 0, 0, null, null, List.of(), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("is not an artifact");
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void fourNewArtifactCreaturesCanPayAllGenericMana() {
        List<Permanent> artifacts = java.util.stream.IntStream.range(0, 4)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new Ornithopter()))
                .toList();
        artifacts.forEach(artifact -> artifact.setSummoningSick(true));
        harness.setHand(player1, List.of(new WindKinRaiders()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                artifacts.stream().map(Permanent::getId).toList());
        assertThat(artifacts).allMatch(Permanent::isTapped);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Wind-Kin Raiders");
    }

    @Test
    void improviseCannotPayBlueMana() {
        List<Permanent> artifacts = java.util.stream.IntStream.range(0, 5)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new Ornithopter()))
                .toList();
        harness.setHand(player1, List.of(new WindKinRaiders()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                artifacts.stream().map(Permanent::getId).toList()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(artifacts).noneMatch(Permanent::isTapped);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void improviseRejectsTappedArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        artifact.tap();
        harness.setHand(player1, List.of(new WindKinRaiders()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void improviseRejectsOpponentsArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player1, List.of(new WindKinRaiders()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    void improviseCannotTapSameArtifactTwice() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.setHand(player1, List.of(new WindKinRaiders()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(artifact.getId(), artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    void flyingPreventsGroundBlocksButAllowsFlyingBlocks() {
        Permanent raiders = addCreatureReady(player1, new WindKinRaiders());
        Permanent groundCreature = addCreatureReady(player2, new DruidOfTheCowl());
        Permanent flyer = addCreatureReady(player2, new Ornithopter());

        assertThat(bls.canBlockAttacker(gd, groundCreature, raiders,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, flyer, raiders,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }
}
