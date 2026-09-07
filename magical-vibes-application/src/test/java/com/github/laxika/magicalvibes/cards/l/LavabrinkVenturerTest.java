package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrayOgre;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LavabrinkVenturer.class, GrizzlyBears.class, GrayOgre.class, LlanowarElves.class})
class LavabrinkVenturerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving awaits an odd/even choice")
    void resolvingAwaitsParityChoice() {
        harness.setHand(player1, List.of(new LavabrinkVenturer()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    @DisplayName("Protection follows the chosen even quality")
    void protectsFromEvenManaValues() {
        Permanent venturer = castAndChoose("EVEN");

        assertThat(gqs.hasProtectionFromSource(gd, venturer, new Permanent(new GrizzlyBears()))).isTrue();
        assertThat(gqs.hasProtectionFromSource(gd, venturer, new Permanent(new LlanowarElves()))).isFalse();
    }

    @Test
    @DisplayName("Protection follows the chosen odd quality")
    void protectsFromOddManaValues() {
        Permanent venturer = castAndChoose("ODD");

        assertThat(gqs.hasProtectionFromSource(gd, venturer, new Permanent(new GrayOgre()))).isTrue();
        assertThat(gqs.hasProtectionFromSource(gd, venturer, new Permanent(new GrizzlyBears()))).isFalse();
    }

    @Test
    @DisplayName("No protection applies before a quality is chosen")
    void hasNoProtectionBeforeChoice() {
        harness.addToBattlefield(player1, new LavabrinkVenturer());
        Permanent venturer = findPermanent(player1, "Lavabrink Venturer");

        assertThat(gqs.hasProtectionFromSource(gd, venturer, new Permanent(new GrizzlyBears()))).isFalse();
        assertThat(gqs.hasProtectionFromSource(gd, venturer, new Permanent(new LlanowarElves()))).isFalse();
    }

    private Permanent castAndChoose(String parity) {
        harness.setHand(player1, List.of(new LavabrinkVenturer()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, parity);

        return findPermanent(player1, "Lavabrink Venturer");
    }
}
