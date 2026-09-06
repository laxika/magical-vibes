package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NorthStar.class, GrizzlyBears.class})
class NorthStarTest extends BaseCardTest {

    @Test
    @DisplayName("Uses colorless mana to pay one colored spell")
    void usesColorlessManaForOneSpell() {
        harness.addToBattlefield(player1, new NorthStar());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Consumes only one permission")
    void consumesOnlyOnePermission() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.addToBattlefield(player1, new NorthStar());
        harness.setHand(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameActionAvailabilityService().isCardPlayable(
                gd, player1.getId(), second, gd.playerManaPools.get(player1.getId()), 0)).isFalse();
    }
}
