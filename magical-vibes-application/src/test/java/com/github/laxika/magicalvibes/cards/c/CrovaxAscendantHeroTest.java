package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.k.KavuPredator;
import com.github.laxika.magicalvibes.cards.s.SaltfieldRecluse;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrovaxAscendantHero.class, KavuPredator.class, SaltfieldRecluse.class})
@DisplayName("Crovax, Ascendant Hero")
class CrovaxAscendantHeroTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts other white creatures and shrinks nonwhite creatures")
    void appliesGlobalCreatureModifiers() {
        Permanent crovax = addCreatureReady(player1, new CrovaxAscendantHero());
        Permanent ownWhiteCreature = addCreatureReady(player1, new SaltfieldRecluse());
        Permanent ownNonwhiteCreature = addCreatureReady(player1, new KavuPredator());
        Permanent whiteCreature = addCreatureReady(player2, new SaltfieldRecluse());
        Permanent nonwhiteCreature = addCreatureReady(player2, new KavuPredator());

        assertThat(gqs.getEffectivePower(gd, crovax)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, crovax)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, ownWhiteCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownWhiteCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownNonwhiteCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownNonwhiteCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, whiteCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, whiteCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, nonwhiteCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, nonwhiteCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Boosts another Crovax but not itself")
    void boostsAnotherCrovax() {
        Permanent ownCrovax = addCreatureReady(player1, new CrovaxAscendantHero());
        Permanent opposingCrovax = addCreatureReady(player2, new CrovaxAscendantHero());

        assertThat(gqs.getEffectivePower(gd, ownCrovax)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ownCrovax)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, opposingCrovax)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, opposingCrovax)).isEqualTo(5);
    }

    @Test
    @DisplayName("Paying 2 life returns Crovax to its owner's hand")
    void returnsToHand() {
        harness.addToBattlefield(player1, new CrovaxAscendantHero());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertInHand(player1, "Crovax, Ascendant Hero");
        harness.assertNotOnBattlefield(player1, "Crovax, Ascendant Hero");
    }

    @Test
    @DisplayName("Cannot pay the ability's life cost from zero life")
    void cannotActivateWithoutEnoughLife() {
        harness.addToBattlefield(player1, new CrovaxAscendantHero());
        harness.setLife(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
    }

    @Test
    @DisplayName("Can activate the ability with exactly 2 life")
    void canActivateWithExactlyTwoLife() {
        harness.addToBattlefield(player1, new CrovaxAscendantHero());
        harness.setLife(player1, 2);

        harness.activateAbility(player1, 0, null, null);

        harness.assertLife(player1, 0);
    }
}
