package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.Absorb;
import com.github.laxika.magicalvibes.cards.k.KavuTitan;
import com.github.laxika.magicalvibes.cards.s.SamiteMinistration;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UrzasRage.class, Absorb.class, SamiteMinistration.class, KavuTitan.class})
class UrzasRageTest extends BaseCardTest {

    @Test
    void dealsThreeDamageWithoutKicker() {
        harness.setHand(player1, List.of(new UrzasRage()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    void kickedDealsTenDamage() {
        harness.setHand(player1, List.of(new UrzasRage()));
        harness.addMana(player1, ManaColor.RED, 12);
        harness.setLife(player2, 20);

        harness.castKickedInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
    }

    @Test
    void kickedDamageCannotBePrevented() {
        UrzasRage rage = new UrzasRage();
        harness.setHand(player1, List.of(rage));
        harness.addMana(player1, ManaColor.RED, 12);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new SamiteMinistration()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castKickedInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, rage.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
    }

    @Test
    void unKickedDamageCanBePrevented() {
        UrzasRage rage = new UrzasRage();
        harness.setHand(player1, List.of(rage));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new SamiteMinistration()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, rage.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(23);
    }

    @Test
    void cannotBeCountered() {
        UrzasRage rage = new UrzasRage();
        harness.setHand(player1, List.of(rage));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new Absorb()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, rage.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void dealsDamageToPermanent() {
        var target = harness.addToBattlefieldAndReturn(player2, new KavuTitan());
        harness.setHand(player1, List.of(new UrzasRage()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Kavu Titan");
    }
}
