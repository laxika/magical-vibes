package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(IllegitimateBusiness.class)
class IllegitimateBusinessTest extends BaseCardTest {

    @Test
    @DisplayName("Illegitimate Business enters tapped and gains its controller 1 life")
    void entersTappedAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new IllegitimateBusiness()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);

        Permanent business = findPermanent(player1, "Illegitimate Business");
        assertThat(business.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Illegitimate Business produces black mana")
    void producesBlackMana() {
        Permanent business = addReadyBusiness(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(business.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Illegitimate Business produces green mana")
    void producesGreenMana() {
        Permanent business = addReadyBusiness(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(business.isTapped()).isTrue();
    }

    private Permanent addReadyBusiness(Player player) {
        Permanent business = new Permanent(new IllegitimateBusiness());
        business.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(business);
        return business;
    }
}
