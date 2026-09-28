package com.github.laxika.magicalvibes.cards.c;

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

@CardUsed({CriminalEnterprise.class, GrizzlyBears.class, Shock.class})
class CriminalEnterpriseTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a Villain token")
    void enteringBattlefieldCreatesVillainToken() {
        castCriminalEnterprise();

        assertThat(findPermanents(player1, "Villain")).hasSize(1);
    }

    @Test
    @DisplayName("A Villain you control dying deals damage to each opponent and gains you life")
    void villainDeathDrainsOpponent() {
        castCriminalEnterprise();
        Permanent villain = findPermanent(player1, "Villain");
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        killWithShock(player1, villain);

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("A non-Villain creature you control dying does not trigger Criminal Enterprise")
    void nonVillainDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new CriminalEnterprise());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        killWithShock(player1, creature);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    private void castCriminalEnterprise() {
        harness.setHand(player1, List.of(new CriminalEnterprise()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void killWithShock(com.github.laxika.magicalvibes.model.Player caster, Permanent creature) {
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
