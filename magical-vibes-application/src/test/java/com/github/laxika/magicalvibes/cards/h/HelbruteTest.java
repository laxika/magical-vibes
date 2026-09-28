package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Helbrute.class, GrizzlyBears.class, Shock.class})
class HelbruteTest extends BaseCardTest {

    @Test
    void canCastFromGraveyardByExilingAnotherCreature() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(new Helbrute(), bears));
        addHelbruteMana();

        harness.castFromGraveyard(player1, 0, List.of(1));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(bears);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Helbrute");
    }

    @Test
    void cannotCastFromGraveyardWithoutAnotherCreature() {
        harness.setGraveyard(player1, List.of(new Helbrute()));
        addHelbruteMana();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Helbrute");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(5);
    }

    @Test
    void cannotExileNoncreatureCardForGraveyardCast() {
        harness.setGraveyard(player1, List.of(new Helbrute(), new Shock()));
        addHelbruteMana();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Helbrute");
    }

    private void addHelbruteMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
