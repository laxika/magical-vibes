package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(VesperGhoul.class)
class VesperGhoulTest extends BaseCardTest {

    @Test
    @DisplayName("Pays 1 life, taps, and adds one mana of a chosen color")
    void paysLifeAndAddsChosenColorMana() {
        Permanent ghoul = addCreatureReady(player1, new VesperGhoul());
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);

        assertThat(ghoul.isTapped()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }
}
