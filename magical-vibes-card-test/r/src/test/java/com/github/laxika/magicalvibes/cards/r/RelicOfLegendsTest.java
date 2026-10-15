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

    @Test
    @DisplayName("Can tap a legendary creature with summoning sickness")
    void tapsSummoningSickLegendaryCreature() {
        harness.addToBattlefield(player1, new RelicOfLegends());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DanithaCapashenParagon());
        creature.setSummoningSick(true);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can use the second ability after tapping Relic for mana")
    void usesSecondAbilityWhileRelicIsTapped() {
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new RelicOfLegends());
        Permanent creature = addCreatureReady(player1, new DanithaCapashenParagon());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(relic.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot tap an already tapped legendary creature for mana")
    void cannotTapTappedLegendaryCreature() {
        harness.addToBattlefield(player1, new RelicOfLegends());
        Permanent creature = addCreatureReady(player1, new DanithaCapashenParagon());
        creature.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot tap an opponent's legendary creature for mana")
    void cannotTapOpponentsLegendaryCreature() {
        harness.addToBattlefield(player1, new RelicOfLegends());
        Permanent creature = addCreatureReady(player2, new DanithaCapashenParagon());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate the first ability twice without untapping Relic")
    void cannotTapRelicTwice() {
        harness.addToBattlefield(player1, new RelicOfLegends());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
