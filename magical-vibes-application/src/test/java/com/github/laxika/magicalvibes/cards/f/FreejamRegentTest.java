package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AegisAutomaton;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FreejamRegent.class, Ornithopter.class, AegisAutomaton.class})
class FreejamRegentTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives Freejam Regent +2/+0 until end of turn")
    void resolvingAbilityBoostsSelf() {
        Permanent regent = addReadyRegent(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(regent.getPowerModifier()).isEqualTo(2);
        assertThat(regent.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The ability can be activated repeatedly for a cumulative boost")
    void repeatedActivationsStack() {
        Permanent regent = addReadyRegent(player1);
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(regent.getPowerModifier()).isEqualTo(4);
        assertThat(regent.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The ability does not require tapping")
    void abilityDoesNotRequireTapping() {
        Permanent regent = addReadyRegent(player1);
        regent.tap();
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The ability cannot be activated without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyRegent(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent regent = addReadyRegent(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(regent.getPowerModifier()).isEqualTo(0);
        assertThat(regent.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Improvise taps an artifact to pay generic mana")
    void improviseTapsArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.setHand(player1, List.of(new FreejamRegent()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreatureTappingPermanents(player1, 0, List.of(artifact.getId()));

        assertThat(artifact.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard() instanceof FreejamRegent);
    }

    @Test
    @DisplayName("Improvise cannot tap a nonartifact permanent")
    void improviseRejectsNonartifact() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FreejamRegent());
        harness.setHand(player1, List.of(new FreejamRegent()));
        harness.addMana(player1, ManaColor.RED, 6);

        assertThatThrownBy(() -> harness.castCreatureTappingPermanents(
                player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("is not an artifact");
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void summoningSickRegentCanActivateAbility() {
        Permanent regent = harness.addToBattlefieldAndReturn(player1, new FreejamRegent());
        regent.setSummoningSick(true);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(regent.getPowerModifier()).isEqualTo(2);
        assertThat(regent.isTapped()).isFalse();
    }

    @Test
    void abilityRequiresRedMana() {
        addReadyRegent(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void fourSummoningSickArtifactsPayAllGenericMana() {
        List<Permanent> artifacts = java.util.stream.IntStream.range(0, 4)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new Ornithopter()))
                .toList();
        artifacts.forEach(artifact -> artifact.setSummoningSick(true));
        harness.setHand(player1, List.of(new FreejamRegent()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreatureTappingPermanents(player1, 0,
                artifacts.stream().map(Permanent::getId).toList());
        harness.passBothPriorities();

        assertThat(artifacts).allMatch(Permanent::isTapped);
        harness.assertOnBattlefield(player1, "Freejam Regent");
    }

    @Test
    void improviseCannotPayRedMana() {
        List<Permanent> artifacts = java.util.stream.IntStream.range(0, 4)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new Ornithopter()))
                .toList();
        harness.setHand(player1, List.of(new FreejamRegent()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureTappingPermanents(player1, 0,
                artifacts.stream().map(Permanent::getId).toList()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void improviseRejectsTappedArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        artifact.tap();
        harness.setHand(player1, List.of(new FreejamRegent()));
        harness.addMana(player1, ManaColor.RED, 6);

        assertThatThrownBy(() -> harness.castCreatureTappingPermanents(
                player1, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    void improviseRejectsOpponentsArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player1, List.of(new FreejamRegent()));
        harness.addMana(player1, ManaColor.RED, 6);

        assertThatThrownBy(() -> harness.castCreatureTappingPermanents(
                player1, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    void flyingRequiresFlyingOrReachToBlock() {
        Permanent regent = addReadyRegent(player1);
        Permanent groundBlocker = harness.addToBattlefieldAndReturn(player2, new AegisAutomaton());
        Permanent flyingBlocker = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        assertThat(harness.getBlockLegalityService().canBlockAttacker(
                gd, groundBlocker, regent, gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(harness.getBlockLegalityService().canBlockAttacker(
                gd, flyingBlocker, regent, gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    private Permanent addReadyRegent(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new FreejamRegent());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
