package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.o.OneWithTheStars;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DonnaNoble.class, GrizzlyBears.class, Shock.class, OneWithTheStars.class})
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
    @DisplayName("Donna Noble reflects her own damage even while unpaired")
    void unpairedDonnaReflectsDamage() {
        Permanent donna = harness.addToBattlefieldAndReturn(player1, new DonnaNoble());

        shock(player2, donna);
        resolveReflectedDamage();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Donna can pair with a creature entering after her")
    void pairsWithLaterEnteringCreature() {
        Permanent donna = harness.addToBattlefieldAndReturn(player1, new DonnaNoble());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(donna.getPairedWithId()).isEqualTo(bears.getId());
        assertThat(bears.getPairedWithId()).isEqualTo(donna.getId());
        shock(player2, bears);
        resolveReflectedDamage();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Declining soulbond leaves Donna unpaired but still able to reflect her own damage")
    void mayDeclinePairing() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DonnaNoble()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        Permanent donna = findPermanent(player1, "Donna Noble");

        assertThat(donna.getPairedWithId()).isNull();
        assertThat(bears.getPairedWithId()).isNull();
        shock(player2, donna);
        resolveReflectedDamage();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Damage to an unrelated creature does not trigger Donna")
    void unrelatedCreatureDamageDoesNotReflect() {
        pairWithDonna();
        Permanent unrelated = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        shock(player2, unrelated);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Donna's trigger still resolves when the damage kills Donna")
    void lethalDamageToDonnaStillReflects() {
        Permanent donna = harness.addToBattlefieldAndReturn(player1, new DonnaNoble());
        shock(player2, donna);
        resolveReflectedDamage();

        shock(player2, donna);
        harness.assertInGraveyard(player1, "Donna Noble");
        resolveReflectedDamage();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Donna becomes unpaired when she stops being a creature")
    void stopsReflectingPartnerDamageWhenDonnaStopsBeingCreature() {
        Permanent bears = pairWithDonna();
        Permanent donna = findPermanent(player1, "Donna Noble");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new OneWithTheStars()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castEnchantment(player2, 0, donna.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, donna)).isFalse();
        assertThat(donna.getPairedWithId()).isNull();
        assertThat(bears.getPairedWithId()).isNull();

        shock(player2, bears);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
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
        harness.castAndResolveInstant(caster, 0, target.getId());
    }

    private void resolveReflectedDamage() {
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
    }
}
