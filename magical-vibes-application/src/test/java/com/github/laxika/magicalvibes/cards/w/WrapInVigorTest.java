package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.Ghostfire;
import com.github.laxika.magicalvibes.cards.h.HorizonCanopy;
import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WrapInVigor.class, NessianCourser.class, Ghostfire.class, HorizonCanopy.class})
class WrapInVigorTest extends BaseCardTest {

    @Test
    @DisplayName("Gives regeneration shields to each creature you control only")
    void regeneratesEachCreatureYouControl() {
        harness.addToBattlefield(player1, new NessianCourser());
        harness.addToBattlefield(player1, new NessianCourser());
        harness.addToBattlefield(player2, new NessianCourser());
        Permanent canopy = harness.addToBattlefieldAndReturn(player1, new HorizonCanopy());
        harness.setHand(player1, List.of(new WrapInVigor()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(findPermanents(player1, "Nessian Courser"))
                .allMatch(permanent -> permanent.getRegenerationShield() == 1);
        assertThat(findPermanent(player2, "Nessian Courser").getRegenerationShield()).isZero();
        assertThat(canopy.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Regeneration shields prevent lethal damage and are spent")
    void regenerationPreventsLethalDamage() {
        Permanent courser = harness.addToBattlefieldAndReturn(player1, new NessianCourser());
        harness.setHand(player1, List.of(new WrapInVigor(), new Ghostfire()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, courser.getId());

        assertThat(findPermanent(player1, "Nessian Courser")).isSameAs(courser);
        assertThat(courser.getRegenerationShield()).isZero();
        assertThat(courser.isTapped()).isTrue();
        assertThat(courser.getMarkedDamage()).isZero();
    }
}
