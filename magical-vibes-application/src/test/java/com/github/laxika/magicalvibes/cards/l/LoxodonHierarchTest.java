package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SelesnyaEvangel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoxodonHierarch.class, SelesnyaEvangel.class, Forest.class, LightningHelix.class})
class LoxodonHierarchTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, you gain 4 life")
    void entersAndGainsLife() {
        harness.setLife(player1, 10);
        harness.castFromHand(player1, new LoxodonHierarch(), "{2}{G}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Sacrificing it regenerates each creature you control")
    void sacrificeRegeneratesOwnCreatures() {
        Permanent hierarch = harness.addToBattlefieldAndReturn(player1, new LoxodonHierarch());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new SelesnyaEvangel());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SelesnyaEvangel());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(hierarch);
        harness.passBothPriorities();

        assertThat(ownCreature.getRegenerationShield()).isEqualTo(1);
        assertThat(ownLand.getRegenerationShield()).isZero();
        assertThat(opponentCreature.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Regeneration shields prevent lethal damage")
    void regenerationSavesOwnCreatureFromLethalDamage() {
        harness.addToBattlefield(player1, new LoxodonHierarch());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new SelesnyaEvangel());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningHelix()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, ownCreature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(ownCreature.getRegenerationShield()).isZero();
        assertThat(ownCreature.getMarkedDamage()).isZero();
    }
}
