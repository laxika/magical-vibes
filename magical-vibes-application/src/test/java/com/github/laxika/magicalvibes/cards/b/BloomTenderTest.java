package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CreakwoodLiege;
import com.github.laxika.magicalvibes.cards.f.FigureOfDestiny;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.PaintersServant;
import com.github.laxika.magicalvibes.cards.s.SpringjackPasture;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.w.WistfulSelkie;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloomTender.class, SuntailHawk.class, FugitiveWizard.class, GrizzlyBears.class,
        HillGiant.class, CreakwoodLiege.class, FigureOfDestiny.class, WistfulSelkie.class,
        SpringjackPasture.class, PaintersServant.class})
class BloomTenderTest extends BaseCardTest {

    /** Adds Bloom Tender at battlefield index 0 with summoning sickness cleared so it can tap. */
    private void addReadyBloomTender() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new BloomTender());
        perm.setSummoningSick(false);
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

    @Test
    @DisplayName("Hybrid permanents contribute both colors, with each color counted once")
    void hybridPermanentsProduceAllFiveColors() {
        addReadyBloomTender();
        harness.addToBattlefield(player1, new CreakwoodLiege());
        harness.addToBattlefield(player1, new FigureOfDestiny());
        harness.addToBattlefield(player1, new WistfulSelkie());

        harness.activateAbility(player1, 0, null, null);

        for (ManaColor color : List.of(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK,
                ManaColor.RED, ManaColor.GREEN)) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        }
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A colorless land contributes no mana, regardless of the mana it can produce")
    void colorlessLandDoesNotContribute() {
        addReadyBloomTender();
        harness.addToBattlefield(player1, new SpringjackPasture());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("A summoning-sick Bloom Tender cannot activate its tap ability")
    void cannotActivateWithSummoningSickness() {
        Permanent tender = harness.addToBattlefieldAndReturn(player1, new BloomTender());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(tender.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Colors granted by Painter's Servant contribute to Bloom Tender's mana")
    void countsColorsGrantedByContinuousEffects() {
        addReadyBloomTender();
        harness.setHand(player1, List.of(new PaintersServant()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
}
