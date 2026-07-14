package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BloomTenderTest extends BaseCardTest {

    /** Adds Bloom Tender at battlefield index 0 with summoning sickness cleared so it can tap. */
    private void addReadyBloomTender() {
        Permanent perm = new Permanent(new BloomTender());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
    }

    @Test
    @DisplayName("Produces one green mana from itself when it is the only permanent")
    void producesGreenFromItself() {
        addReadyBloomTender();

        harness.activateAbility(player1, 0, null, null);

        // Bloom Tender is green, so at least {G} is produced; no choice — one of each color.
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Adds one mana of each color among permanents controlled, simultaneously")
    void addsOneOfEachColor() {
        addReadyBloomTender();                                   // green
        harness.addToBattlefield(player1, new SuntailHawk());    // white
        harness.addToBattlefield(player1, new FugitiveWizard()); // blue

        harness.activateAbility(player1, 0, null, null);

        // All colors added at once, no color-choice prompt.
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A color shared by several permanents produces only one mana of that color")
    void duplicateColorsCountOnce() {
        addReadyBloomTender();                                // green
        harness.addToBattlefield(player1, new GrizzlyBears()); // green

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Permanents controlled by opponents do not contribute colors")
    void opponentPermanentsDoNotContribute() {
        addReadyBloomTender();                              // green
        harness.addToBattlefield(player2, new HillGiant()); // opponent's red

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate the tap ability again while tapped")
    void cannotActivateWhileTapped() {
        addReadyBloomTender();

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }
}
