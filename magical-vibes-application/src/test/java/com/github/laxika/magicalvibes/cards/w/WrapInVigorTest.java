package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WrapInVigor.class, GrizzlyBears.class, Shock.class})
class WrapInVigorTest extends BaseCardTest {

    @Test
    @DisplayName("Gives regeneration shields to each creature you control only")
    void regeneratesEachCreatureYouControl() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WrapInVigor()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears"))
                .allMatch(permanent -> permanent.getRegenerationShield() == 1);
        assertThat(findPermanent(player2, "Grizzly Bears").getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Regeneration shields prevent lethal damage and are spent")
    void regenerationPreventsLethalDamage() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WrapInVigor(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears")).isSameAs(bears);
        assertThat(bears.getRegenerationShield()).isZero();
    }
}
