package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ProphetOfDistortion.class, GrizzlyBears.class})
class ProphetOfDistortionTest extends BaseCardTest {

    @Test
    @DisplayName("Pays three generic and one colorless mana to draw a card")
    void paysAbilityCostAndDrawsCard() {
        addReadyProphet();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot pay the colorless symbol with colored mana")
    void requiresColorlessMana() {
        addReadyProphet();
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyProphet() {
        Permanent prophet = new Permanent(new ProphetOfDistortion());
        prophet.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(prophet);
        return prophet;
    }
}
