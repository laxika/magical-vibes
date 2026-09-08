package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YotianSoldier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DaruWarchief.class, GrizzlyBears.class, YotianSoldier.class})
class DaruWarchiefTest extends BaseCardTest {

    @Test
    @DisplayName("Soldier creatures you control get +1/+2, including Daru Warchief")
    void boostsOwnSoldiers() {
        harness.addToBattlefield(player1, new DaruWarchief());
        harness.addToBattlefield(player1, new YotianSoldier());

        Permanent warchief = findPermanent(player1, "Daru Warchief");
        Permanent soldier = findPermanent(player1, "Yotian Soldier");

        assertThat(gqs.getEffectivePower(gd, warchief)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, warchief)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(6);
    }

    @Test
    @DisplayName("Does not boost non-Soldiers or an opponent's Soldiers")
    void onlyBoostsOwnSoldiers() {
        harness.addToBattlefield(player1, new DaruWarchief());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new YotianSoldier());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        Permanent opponentSoldier = findPermanent(player2, "Yotian Soldier");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentSoldier)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentSoldier)).isEqualTo(4);
    }

    @Test
    @DisplayName("Soldier spells you cast cost {1} less to cast")
    void reducesOwnSoldierSpellCost() {
        harness.addToBattlefield(player1, new DaruWarchief());
        harness.setHand(player1, List.of(new YotianSoldier()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cost reduction does not apply to non-Soldier spells")
    void doesNotReduceNonSoldierSpellCost() {
        harness.addToBattlefield(player1, new DaruWarchief());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cost reduction does not apply to an opponent's Soldier spells")
    void doesNotReduceOpponentSoldierSpellCost() {
        harness.addToBattlefield(player1, new DaruWarchief());
        harness.setHand(player2, List.of(new YotianSoldier()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
