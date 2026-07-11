package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpringleafDrumTest extends BaseCardTest {

    @Test
    @DisplayName("Taps itself and the only creature, then adds one mana of the chosen color")
    void tapsCreatureAndAddsChosenColorMana() {
        harness.addToBattlefield(player1, new SpringleafDrum());
        Permanent drum = gd.playerBattlefields.get(player1.getId()).get(0);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

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
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

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
}
