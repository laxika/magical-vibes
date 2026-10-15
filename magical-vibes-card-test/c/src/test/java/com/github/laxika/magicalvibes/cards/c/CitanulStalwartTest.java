package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.EnergyRefractor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CitanulStalwart.class, EnergyRefractor.class, Forest.class})
class CitanulStalwartTest extends BaseCardTest {

    @Test
    @DisplayName("Taps an untapped creature and adds one chosen-color mana")
    void tapsCreatureAndAddsMana() {
        Permanent stalwart = addCreatureReady(player1, new CitanulStalwart());
        Permanent support = addCreatureReady(player1, new CitanulStalwart());

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(stalwart.isTapped()).isTrue();
        assertThat(support.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taps an untapped artifact and adds one chosen-color mana")
    void tapsArtifactAndAddsMana() {
        Permanent stalwart = addCreatureReady(player1, new CitanulStalwart());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new EnergyRefractor());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(stalwart.isTapped()).isTrue();
        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without another untapped artifact or creature")
    void cannotActivateWithoutAnotherArtifactOrCreature() {
        addCreatureReady(player1, new CitanulStalwart());
        harness.addToBattlefield(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Can tap a summoning-sick support creature and produces any color without using the stack")
    void tapsSummoningSickSupportAndProducesAnyColor(ManaColor color) {
        Permanent stalwart = addCreatureReady(player1, new CitanulStalwart());
        Permanent support = harness.addToBattlefieldAndReturn(player1, new CitanulStalwart());
        support.setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(stalwart.isTapped()).isTrue();
        assertThat(support.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        for (ManaColor manaColor : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor))
                    .isEqualTo(manaColor == color ? 1 : 0);
        }
    }

    @Test
    @DisplayName("A summoning-sick Stalwart cannot activate its own tap ability")
    void summoningSickSourceCannotActivate() {
        Permanent stalwart = harness.addToBattlefieldAndReturn(player1, new CitanulStalwart());
        stalwart.setSummoningSick(true);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new EnergyRefractor());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(stalwart.isTapped()).isFalse();
        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Already tapped artifacts and creatures cannot pay the additional cost")
    void tappedSupportCannotPayCost() {
        addCreatureReady(player1, new CitanulStalwart());
        Permanent creature = addCreatureReady(player1, new CitanulStalwart());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new EnergyRefractor());
        creature.tap();
        artifact.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opponent-controlled artifacts and creatures cannot pay the additional cost")
    void opponentPermanentsCannotPayCost() {
        addCreatureReady(player1, new CitanulStalwart());
        Permanent creature = addCreatureReady(player2, new CitanulStalwart());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new EnergyRefractor());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped Stalwart cannot activate even with an untapped support artifact")
    void tappedSourceCannotActivate() {
        Permanent stalwart = addCreatureReady(player1, new CitanulStalwart());
        stalwart.tap();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new EnergyRefractor());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(artifact.isTapped()).isFalse();
    }
}
