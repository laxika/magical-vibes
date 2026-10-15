package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
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

@CardUsed({SpringleafDrum.class, WoodlandChangeling.class, MarchOfTheMachines.class})
class SpringleafDrumTest extends BaseCardTest {

    @Test
    @DisplayName("Taps itself and the only creature, then adds one mana of the chosen color")
    void tapsCreatureAndAddsChosenColorMana() {
        Permanent drum = harness.addToBattlefieldAndReturn(player1, new SpringleafDrum());
        Permanent bears = addCreatureReady(player1, new WoodlandChangeling());

        harness.activateAbility(player1, 0, null, null);

        // Only one untapped creature -> auto-tapped, then prompted for the mana color
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(drum.isTapped()).isTrue();
        assertThat(bears.isTapped()).isTrue();
        // Mana ability -> does not use the stack
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Prompts which creature to tap when multiple are available")
    void promptsForCreatureChoiceWithMultipleCreatures() {
        harness.addToBattlefield(player1, new SpringleafDrum());
        Permanent bears = addCreatureReady(player1, new WoodlandChangeling());
        addCreatureReady(player1, new WoodlandChangeling());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without an untapped creature to tap")
    void cannotActivateWithoutUntappedCreature() {
        harness.addToBattlefield(player1, new SpringleafDrum());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Can tap a summoning-sick creature to produce any of the five colors")
    void canTapSummoningSickCreatureForAnyColor(ManaColor color) {
        Permanent drum = harness.addToBattlefieldAndReturn(player1, new SpringleafDrum());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());
        creature.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(drum.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot tap an already tapped creature to pay the cost")
    void cannotActivateWithOnlyTappedCreature() {
        Permanent drum = harness.addToBattlefieldAndReturn(player1, new SpringleafDrum());
        Permanent creature = addCreatureReady(player1, new WoodlandChangeling());
        creature.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(drum.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot tap an opponent's creature to pay the cost")
    void cannotActivateWithOnlyOpponentsCreature() {
        Permanent drum = harness.addToBattlefieldAndReturn(player1, new SpringleafDrum());
        Permanent creature = addCreatureReady(player2, new WoodlandChangeling());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(drum.isTapped()).isFalse();
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate an already tapped Drum even with an untapped creature")
    void cannotActivateTappedDrum() {
        Permanent drum = harness.addToBattlefieldAndReturn(player1, new SpringleafDrum());
        Permanent creature = addCreatureReady(player1, new WoodlandChangeling());
        drum.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An animated Drum cannot pay both tap costs by tapping itself")
    void animatedDrumCannotPayBothTapCostsWithItself() {
        Permanent drum = harness.addToBattlefieldAndReturn(player1, new SpringleafDrum());
        drum.setSummoningSick(false);
        harness.addToBattlefield(player1, new MarchOfTheMachines());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(drum.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
