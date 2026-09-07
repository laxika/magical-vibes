package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AdamantWill;
import com.github.laxika.magicalvibes.cards.a.AzimaetDrake;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        KaerveksPurge.class,
        AdamantWill.class,
        AzimaetDrake.class,
        Forest.class
})
class KaerveksPurgeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target creature with mana value X and deals its power to its controller")
    void destroysCreatureAndDealsPowerDamage() {
        harness.addToBattlefield(player2, new AzimaetDrake()); // mana value 3, 1/3
        UUID target = harness.getPermanentId(player2, "Azimaet Drake");
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.setHand(player1, List.of(new KaerveksPurge()));
        harness.addMana(player1, ManaColor.BLACK, 4); // {X=3}{B}{R}
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, 3, target);

        harness.assertInGraveyard(player2, "Azimaet Drake");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Deals no damage when the creature survives destruction")
    void indestructibleCreatureTakesNoDamage() {
        harness.addToBattlefield(player2, new AzimaetDrake());
        UUID target = harness.getPermanentId(player2, "Azimaet Drake");
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.setHand(player1, List.of(new AdamantWill(), new KaerveksPurge()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target);
        harness.castAndResolveSorcery(player1, 0, 3, target);

        harness.assertOnBattlefield(player2, "Azimaet Drake");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Cannot target a creature whose mana value does not equal X")
    void cannotTargetCreatureWithDifferentManaValue() {
        harness.addToBattlefield(player2, new AzimaetDrake()); // mana value 3
        UUID target = harness.getPermanentId(player2, "Azimaet Drake");

        harness.setHand(player1, List.of(new KaerveksPurge()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        UUID target = harness.getPermanentId(player2, "Forest");

        harness.setHand(player1, List.of(new KaerveksPurge()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, target))
                .isInstanceOf(IllegalStateException.class);
    }
}
