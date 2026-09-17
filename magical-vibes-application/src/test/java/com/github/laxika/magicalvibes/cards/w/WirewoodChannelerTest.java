package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DefiantElf;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WirewoodChanneler.class, DefiantElf.class, FugitiveWizard.class})
class WirewoodChannelerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Wirewood Channeler prompts for a mana color")
    void activateAbilityPromptsManaColor() {
        Permanent channeler = addCreatureReady(player1, new WirewoodChanneler());

        harness.activateAbility(player1, 0, null, null);

        assertThat(channeler.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    @DisplayName("Counts Wirewood Channeler itself as an Elf")
    void countsItselfAsAnElf() {
        addCreatureReady(player1, new WirewoodChanneler());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Adds one mana of the chosen color for each Elf on the battlefield")
    void addsManaForEachElfOnTheBattlefield() {
        addCreatureReady(player1, new WirewoodChanneler());
        harness.addToBattlefield(player1, new DefiantElf());
        harness.addToBattlefield(player2, new DefiantElf());
        harness.addToBattlefield(player1, new FugitiveWizard());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Cannot activate Wirewood Channeler with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new WirewoodChanneler());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }
}
