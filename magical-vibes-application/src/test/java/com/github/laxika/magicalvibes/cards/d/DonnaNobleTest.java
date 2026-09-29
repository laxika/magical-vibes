package com.github.laxika.magicalvibes.cards.d;

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

@CardUsed({DonnaNoble.class, GrizzlyBears.class, Shock.class})
class DonnaNobleTest extends BaseCardTest {

    @Test
    @DisplayName("Donna Noble deals the damage received by her paired creature to an opponent")
    void pairedCreatureDamageIsReflectedByDonna() {
        Permanent bears = pairWithDonna();

        shock(player2, bears);
        resolveReflectedDamage();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Donna Noble deals damage to an opponent when Donna is dealt damage")
    void donnaDamageIsReflected() {
        pairWithDonna();
        Permanent donna = findPermanent(player1, "Donna Noble");

        shock(player2, donna);
        resolveReflectedDamage();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Donna Noble does not trigger while unpaired")
    void unpairedDonnaDoesNotReflectDamage() {
        Permanent donna = harness.addToBattlefieldAndReturn(player1, new DonnaNoble());

        shock(player2, donna);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private Permanent pairWithDonna() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DonnaNoble()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        return bears;
    }

    private void shock(com.github.laxika.magicalvibes.model.Player caster, Permanent target) {
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities();
    }

    private void resolveReflectedDamage() {
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
    }
}
