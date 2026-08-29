package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OminousAsylum.class, GrizzlyBears.class})
class OminousAsylumTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        Card card = new OminousAsylum();
        harness.setHand(player1, List.of(card));

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == card && permanent.isTapped());
    }

    @Test
    @DisplayName("Adds black mana")
    void addsBlackMana() {
        addsMana(ManaColor.BLACK);
    }

    @Test
    @DisplayName("Adds red mana")
    void addsRedMana() {
        addsMana(ManaColor.RED);
    }

    @Test
    @DisplayName("Pays four mana and taps to surveil 1")
    void paysFourManaAndSurveils() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        Permanent land = addLandReady();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(land.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    private Permanent addLandReady() {
        Permanent land = new Permanent(new OminousAsylum());
        land.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(land);
        return land;
    }

    private void addsMana(ManaColor color) {
        Permanent land = addLandReady();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }
}
