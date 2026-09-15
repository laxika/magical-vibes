package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GaeasHerald;
import com.github.laxika.magicalvibes.cards.m.ManaCylix;
import com.github.laxika.magicalvibes.cards.s.SeaSnidd;
import com.github.laxika.magicalvibes.cards.s.SkyshroudBlessing;
import com.github.laxika.magicalvibes.cards.v.VolcanoImp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunscapeFamiliar.class, GaeasHerald.class, SeaSnidd.class, VolcanoImp.class,
        ManaCylix.class, SkyshroudBlessing.class})
class SunscapeFamiliarTest extends BaseCardTest {

    @Test
    @DisplayName("Green spells you cast cost {1} less")
    void greenSpellsCostOneLess() {
        harness.addToBattlefield(player1, new SunscapeFamiliar());
        harness.setHand(player1, List.of(new GaeasHerald()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Gaea's Herald"));
    }

    @Test
    @DisplayName("Blue spells you cast cost {1} less")
    void blueSpellsCostOneLess() {
        harness.addToBattlefield(player1, new SunscapeFamiliar());
        harness.setHand(player1, List.of(new SeaSnidd()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Sea Snidd"));
    }

    @Test
    @DisplayName("Spells of other colors are not reduced")
    void otherColorsAreNotReduced() {
        harness.addToBattlefield(player1, new SunscapeFamiliar());
        harness.setHand(player1, List.of(new VolcanoImp()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Colorless spells are not reduced")
    void colorlessSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new SunscapeFamiliar());
        harness.setHand(player1, List.of(new ManaCylix()));

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction applies to noncreature green spells")
    void noncreatureGreenSpellsCostOneLess() {
        harness.addToBattlefield(player1, new SunscapeFamiliar());
        harness.setHand(player1, List.of(new SkyshroudBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Skyshroud Blessing"));
    }

    @Test
    @DisplayName("The reduction only applies to the controller's spells")
    void opponentSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new SunscapeFamiliar());
        harness.setHand(player2, List.of(new GaeasHerald()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
