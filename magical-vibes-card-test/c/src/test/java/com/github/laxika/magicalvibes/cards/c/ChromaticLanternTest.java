package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SunderingGrowth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChromaticLantern.class, Forest.class, SunderingGrowth.class})
class ChromaticLanternTest extends BaseCardTest {

    @Test
    void lanternAddsManaOfAnyColor() {
        harness.addToBattlefield(player1, new ChromaticLantern());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    void ownLandsGainAnyColorManaAbility() {
        harness.addToBattlefield(player1, new ChromaticLantern());
        harness.addToBattlefield(player1, new Forest());

        Permanent forest = gd.playerBattlefields.get(player1.getId()).get(1);
        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void opponentLandsDoNotGainAbility() {
        harness.addToBattlefield(player1, new ChromaticLantern());
        harness.addToBattlefield(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid ability index");
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void lanternProducesEachColorWithoutUsingTheStack(ManaColor color) {
        harness.addToBattlefield(player1, new ChromaticLantern());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void grantedLandAbilityProducesEachColorWhileLanternIsTapped(ManaColor color) {
        harness.addToBattlefield(player1, new ChromaticLantern());
        harness.addToBattlefield(player1, new Forest());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color))
                .isEqualTo(color == ManaColor.BLUE ? 2 : 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    void forestRetainsItsOriginalManaAbility() {
        harness.addToBattlefield(player1, new ChromaticLantern());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).get(1).isTapped()).isTrue();
    }

    @Test
    void landsLoseGrantedAbilityWhenLanternLeavesBattlefield() {
        harness.addToBattlefield(player1, new ChromaticLantern());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player2, List.of(new SunderingGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Chromatic Lantern"));

        harness.assertInGraveyard(player1, "Chromatic Lantern");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid ability index");
        harness.tapPermanent(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }
}
