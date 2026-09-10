package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.Propaganda;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SapphireMedallion.class, Propaganda.class, WindDrake.class, TrainedArmodon.class})
class SapphireMedallionTest extends BaseCardTest {

    @Test
    @DisplayName("Blue spells you cast cost {1} less")
    void blueSpellsCostOneLess() {
        harness.addToBattlefield(player1, new SapphireMedallion());
        // Wind Drake costs {2}{U}; with the {1} reduction two mana is enough
        harness.setHand(player1, List.of(new WindDrake()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(e -> e.getCard().getName().equals("Wind Drake"));
    }

    @Test
    @DisplayName("Non-blue spells are not reduced")
    void nonBlueSpellsNotReduced() {
        harness.addToBattlefield(player1, new SapphireMedallion());
        // Trained Armodon costs {1}{G}{G}; not blue, so two green mana are not enough
        harness.setHand(player1, List.of(new TrainedArmodon()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction only applies to the controller's spells")
    void opponentSpellsNotReduced() {
        harness.addToBattlefield(player1, new SapphireMedallion());
        harness.setHand(player2, List.of(new WindDrake()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction does not reduce colored mana")
    void reductionOnlyReducesGenericMana() {
        harness.addToBattlefield(player1, new SapphireMedallion());
        harness.setHand(player1, List.of(new WindDrake()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction applies to blue noncreature spells")
    void blueNoncreatureSpellsCostOneLess() {
        harness.addToBattlefield(player1, new SapphireMedallion());
        harness.setHand(player1, List.of(new Propaganda()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).anyMatch(e -> e.getCard().getName().equals("Propaganda"));
    }

    @Test
    @DisplayName("Multiple reductions stack")
    void multipleMedallionsStack() {
        harness.addToBattlefield(player1, new SapphireMedallion());
        harness.addToBattlefield(player1, new SapphireMedallion());
        harness.setHand(player1, List.of(new WindDrake()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(e -> e.getCard().getName().equals("Wind Drake"));
    }
}
