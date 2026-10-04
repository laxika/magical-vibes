package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WurmsTooth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FoundryAssembler.class, GrizzlyBears.class, WurmsTooth.class})
class FoundryAssemblerTest extends BaseCardTest {

    @Test
    @DisplayName("Improvise taps an artifact to pay generic mana")
    void improviseTapsArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new WurmsTooth());
        harness.setHand(player1, List.of(new FoundryAssembler()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(artifact.getId()));

        assertThat(artifact.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Foundry Assembler")).isNotNull();
    }

    @Test
    @DisplayName("Improvise cannot tap a nonartifact permanent")
    void improviseRejectsNonartifact() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FoundryAssembler()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> gs.playCard(
                gd, player1, 0, 0, null, null, List.of(), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("is not an artifact");
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Improvise can pay the entire cost with summoning-sick artifact creatures")
    void improvisePaysEntireCost() {
        List<Permanent> artifacts = IntStream.range(0, 5)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new FoundryAssembler()))
                .toList();
        artifacts.forEach(artifact -> artifact.setSummoningSick(true));
        FoundryAssembler spell = new FoundryAssembler();
        harness.setHand(player1, List.of(spell));

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                artifacts.stream().map(Permanent::getId).toList());

        assertThat(artifacts).allMatch(Permanent::isTapped);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(spell.getId()));
    }

    @Test
    @DisplayName("Improvise cannot use an already tapped artifact")
    void improviseRejectsTappedArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FoundryAssembler());
        artifact.tap();
        harness.setHand(player1, List.of(new FoundryAssembler()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> gs.playCard(
                gd, player1, 0, 0, null, null, List.of(), List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        harness.assertInHand(player1, "Foundry Assembler");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Improvise cannot use an opponent's artifact")
    void improviseRejectsOpponentsArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FoundryAssembler());
        harness.setHand(player1, List.of(new FoundryAssembler()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> gs.playCard(
                gd, player1, 0, 0, null, null, List.of(), List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not found on your battlefield");
        assertThat(artifact.isTapped()).isFalse();
        harness.assertInHand(player1, "Foundry Assembler");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Improvise cannot count the same artifact twice")
    void improviseRejectsDuplicateArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FoundryAssembler());
        harness.setHand(player1, List.of(new FoundryAssembler()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> gs.playCard(
                gd, player1, 0, 0, null, null, List.of(), List.of(artifact.getId(), artifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be tapped twice");
        assertThat(artifact.isTapped()).isFalse();
        harness.assertInHand(player1, "Foundry Assembler");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Improvise is optional when the full mana cost is paid")
    void canCastWithoutImprovise() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FoundryAssembler());

        harness.castFromHand(player1, new FoundryAssembler(), "{5}");

        assertThat(artifact.isTapped()).isFalse();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }
}
