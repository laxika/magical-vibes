package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KefnetTheMindful;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Rivendell.class, KefnetTheMindful.class, GrizzlyBears.class})
class RivendellTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped without a legendary creature")
    void entersTappedWithoutLegendaryCreature() {
        harness.setHand(player1, List.of(new Rivendell()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Rivendell").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control a legendary creature")
    void entersUntappedWithLegendaryCreature() {
        harness.addToBattlefield(player1, new KefnetTheMindful());
        harness.setHand(player1, List.of(new Rivendell()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Rivendell").isTapped()).isFalse();
    }

    @Test
    @DisplayName("The Scry 2 ability requires a legendary creature")
    void scryAbilityRequiresLegendaryCreature() {
        harness.addToBattlefield(player1, new Rivendell());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The Scry 2 ability opens a scry interaction")
    void scryAbilityOpensInteraction() {
        harness.addToBattlefield(player1, new Rivendell());
        harness.addToBattlefield(player1, new KefnetTheMindful());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }
}
