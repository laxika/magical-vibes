package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CanopySpider;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmeraldMedallion.class, CanopySpider.class, WindDrake.class, Earthcraft.class})
class EmeraldMedallionTest extends BaseCardTest {

    @Test
    @DisplayName("Green spells you cast cost {1} less")
    void greenSpellsCostOneLess() {
        harness.addToBattlefield(player1, new EmeraldMedallion());
        // Canopy Spider costs {1}{G} — with the {1} reduction it should cost just {G}
        harness.setHand(player1, List.of(new CanopySpider()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Non-green spells are not reduced")
    void nonGreenSpellsNotReduced() {
        harness.addToBattlefield(player1, new EmeraldMedallion());
        // Wind Drake costs {2}{U} — not green, so only {2}{U} without colorless mana is not enough
        harness.setHand(player1, List.of(new WindDrake()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction only applies to the controller's spells")
    void opponentSpellsNotReduced() {
        harness.addToBattlefield(player1, new EmeraldMedallion());
        harness.setHand(player2, List.of(new CanopySpider()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Green noncreature spells you cast cost {1} less")
    void greenNoncreatureSpellsCostOneLess() {
        harness.addToBattlefield(player1, new EmeraldMedallion());
        harness.setHand(player1, List.of(new Earthcraft()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }
}
