package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoxodonHierarch.class, GrizzlyBears.class})
class LoxodonHierarchTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, you gain 4 life")
    void entersAndGainsLife() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new LoxodonHierarch()));
        addLoxodonHierarchMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Sacrificing it regenerates each creature you control")
    void sacrificeRegeneratesOwnCreatures() {
        Permanent hierarch = harness.addToBattlefieldAndReturn(player1, new LoxodonHierarch());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(hierarch);
        harness.passBothPriorities();

        assertThat(ownCreature.getRegenerationShield()).isEqualTo(1);
        assertThat(opponentCreature.getRegenerationShield()).isZero();
    }

    private void addLoxodonHierarchMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
