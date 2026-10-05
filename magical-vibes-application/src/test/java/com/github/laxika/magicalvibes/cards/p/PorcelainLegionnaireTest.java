package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.i.ImmolatingSouleater;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PorcelainLegionnaire.class, ImmolatingSouleater.class})
class PorcelainLegionnaireTest extends BaseCardTest {

    @Test
    void castsWithWhiteManaWithoutPayingLife() {
        harness.setHand(player1, List.of(new PorcelainLegionnaire()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Porcelain Legionnaire");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void castsWithTwoGenericManaAndTwoLife() {
        harness.setHand(player1, List.of(new PorcelainLegionnaire()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Porcelain Legionnaire");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    void canSpendWhiteManaOnGenericCostAndPayLifeForPhyrexianSymbol() {
        harness.setHand(player1, List.of(new PorcelainLegionnaire()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Porcelain Legionnaire");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    void cannotPayPhyrexianSymbolWithLessThanTwoLife() {
        harness.setHand(player1, List.of(new PorcelainLegionnaire()));
        harness.setLife(player1, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void attackingFirstStrikerKillsBlockerBeforeRegularDamage() {
        Permanent attacker = addCreatureReady(player1, new PorcelainLegionnaire());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ImmolatingSouleater());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertOnBattlefield(player1, "Porcelain Legionnaire");
        harness.assertInGraveyard(player2, "Immolating Souleater");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void blockingFirstStrikerKillsAttackerBeforeRegularDamage() {
        Permanent attacker = addCreatureReady(player2, new ImmolatingSouleater());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player1, new PorcelainLegionnaire());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Porcelain Legionnaire");
        harness.assertInGraveyard(player2, "Immolating Souleater");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void opposingFirstStrikersDealDamageSimultaneously() {
        Permanent attacker = addCreatureReady(player1, new PorcelainLegionnaire());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new PorcelainLegionnaire());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertInGraveyard(player1, "Porcelain Legionnaire");
        harness.assertInGraveyard(player2, "Porcelain Legionnaire");
    }

    @Test
    void unblockedFirstStrikerDealsDamageOnlyOnce() {
        Permanent attacker = addCreatureReady(player1, new PorcelainLegionnaire());
        attacker.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }
}
