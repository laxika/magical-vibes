package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UndercellarMyconid.class, WrathOfGod.class})
class UndercellarMyconidTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a Saproling")
    void enteringCreatesSaproling() {
        castAndResolveMyconid();

        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
    }

    @Test
    @DisplayName("Dying creates a Saproling")
    void dyingCreatesSaproling() {
        harness.addToBattlefield(player1, new UndercellarMyconid());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
    }

    @Test
    @DisplayName("Tapping adds one mana of any color")
    void tapsForAnyColor() {
        Permanent myconid = addCreatureReady(player1, new UndercellarMyconid());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(myconid.isTapped()).isTrue();
    }

    private void castAndResolveMyconid() {
        harness.setHand(player1, List.of(new UndercellarMyconid()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
