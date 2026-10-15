package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.t.TheRoyalScions;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlayingFire.class, AirElemental.class, TheRoyalScions.class})
class SlayingFireTest extends BaseCardTest {

    @Test
    void dealsThreeDamageWithoutAdamant() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new SlayingFire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Air Elemental");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    void dealsFourDamageWithAdamant() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new SlayingFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Air Elemental");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    void canDealDamageToAPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SlayingFire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    void twoRedManaDoesNotEnableAdamant() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SlayingFire()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 17);
    }

    @Test
    void adamantDealsFourDamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SlayingFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 16);
    }

    @Test
    void redManaAddedAfterCastingDoesNotEnableAdamant() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new SlayingFire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, player1.getId());

        harness.addMana(player1, ManaColor.RED, 3);
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
    }

    @Test
    void adamantRemovesFourLoyaltyFromPlaneswalker() {
        var planeswalker = harness.enterBattlefieldAndReturn(player2, new TheRoyalScions());
        harness.setHand(player1, List.of(new SlayingFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, planeswalker.getId());

        harness.assertOnBattlefield(player2, "The Royal Scions");
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertLife(player2, 20);
    }
}
