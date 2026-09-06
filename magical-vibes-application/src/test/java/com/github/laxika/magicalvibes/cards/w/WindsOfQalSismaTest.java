package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WindsOfQalSisma.class, AirElemental.class, GrizzlyBears.class})
class WindsOfQalSismaTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents all combat damage without ferocious")
    void preventsAllCombatDamageWithoutFerocious() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        castWinds();

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Ferocious allows creatures you control to deal combat damage")
    void ferociousAllowsYourCreaturesToDealCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new AirElemental());
        castWinds();

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Ferocious prevents combat damage from creatures opponents control")
    void ferociousPreventsOpponentsCombatDamage() {
        addCreatureReady(player1, new AirElemental());
        addCreatureReady(player2, new GrizzlyBears());
        castWinds();

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    private void castWinds() {
        harness.setHand(player1, List.of(new WindsOfQalSisma()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);
    }
}
