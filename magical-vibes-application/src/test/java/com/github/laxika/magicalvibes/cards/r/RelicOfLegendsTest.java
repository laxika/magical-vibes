package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DanithaCapashenParagon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RelicOfLegends.class, DanithaCapashenParagon.class, GrizzlyBears.class})
class RelicOfLegendsTest extends BaseCardTest {

    @Test
    @DisplayName("Taps for one mana of any color")
    void tapsForAnyColor() {
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new RelicOfLegends());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(relic.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Taps an untapped legendary creature for one mana of any color")
    void tapsLegendaryCreatureForAnyColor() {
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new RelicOfLegends());
        Permanent legendaryCreature = addCreatureReady(player1, new DanithaCapashenParagon());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(relic.isTapped()).isFalse();
        assertThat(legendaryCreature.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot use the second ability without an untapped legendary creature")
    void cannotTapNonLegendaryCreatureForMana() {
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new RelicOfLegends());
        Permanent nonLegendaryCreature = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
        assertThat(relic.isTapped()).isFalse();
        assertThat(nonLegendaryCreature.isTapped()).isFalse();
    }
}
