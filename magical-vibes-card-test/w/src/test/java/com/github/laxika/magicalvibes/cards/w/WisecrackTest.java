package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FireServant;
import com.github.laxika.magicalvibes.cards.k.KravensCats;
import com.github.laxika.magicalvibes.cards.l.LurkingLizards;
import com.github.laxika.magicalvibes.cards.m.MorbiusTheLivingVampire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Wisecrack.class, KravensCats.class, Forest.class, LurkingLizards.class,
        MorbiusTheLivingVampire.class, FireServant.class})
class WisecrackTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the target creature's power to itself")
    void dealsPowerDamageToTargetCreature() {
        Permanent target = addCreatureReady(player2, new KravensCats());

        castWisecrack(target);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Deals 2 damage to the controller when the target creature is attacking")
    void attackingTargetAlsoDamagesItsController() {
        Permanent target = addCreatureReady(player1, new KravensCats());
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new Wisecrack()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        declareAttackers(List.of(0));
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Can target only a creature")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new Wisecrack()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Permanent target = findPermanent(player2, "Forest");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void selfDamageUsesTheCreaturesLifelink() {
        Permanent target = addCreatureReady(player2, new MorbiusTheLivingVampire());
        harness.setLife(player2, 10);

        castWisecrack(target);

        harness.assertLife(player2, 13);
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player2, "Morbius the Living Vampire");
    }

    @Test
    void spellDamageMultiplierDoesNotMultiplyCreatureSelfDamage() {
        harness.addToBattlefield(player1, new FireServant());
        Permanent target = addCreatureReady(player2, new LurkingLizards());

        castWisecrack(target);

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Lurking Lizards");
        harness.assertLife(player2, 20);
    }

    @Test
    void spellDamageMultiplierAppliesToAttackingCreaturesControllerOnly() {
        harness.addToBattlefield(player1, new FireServant());
        Permanent target = addCreatureReady(player2, new LurkingLizards());
        prepareWisecrack();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(0)));

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player2, 16);
        harness.assertLife(player1, 20);
    }

    @Test
    void usesPowerAtResolutionAfterCreatureIsPumpedInResponse() {
        Permanent target = addCreatureReady(player2, new KravensCats());
        prepareWisecrack();
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.castInstant(player1, 0, target.getId());
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertInGraveyard(player2, "Kraven's Cats");
        harness.assertLife(player2, 20);
    }

    private void prepareWisecrack() {
        harness.setHand(player1, List.of(new Wisecrack()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void castWisecrack(Permanent target) {
        prepareWisecrack();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
