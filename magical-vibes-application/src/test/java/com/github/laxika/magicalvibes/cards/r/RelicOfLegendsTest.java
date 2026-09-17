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
    @DisplayName("Tapping Relic of Legends adds one mana of the chosen color")
    void tapsRelicForMana() {
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new RelicOfLegends());

        harness.activateAbility(player1, battlefieldIndex(relic), 0, null, null);

        assertThat(relic.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping an untapped legendary creature adds one mana")
    void tapsLegendaryCreatureForMana() {
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new RelicOfLegends());
        Permanent legendary = addCreatureReady(player1, new DanithaCapashenParagon());

        harness.activateAbility(player1, battlefieldIndex(relic), 1, null, null);

        assertThat(relic.isTapped()).isFalse();
        assertThat(legendary.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.WHITE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second ability requires an untapped legendary creature you control")
    void secondAbilityRequiresMatchingCreature() {
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new RelicOfLegends());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(relic), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents to tap");
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
