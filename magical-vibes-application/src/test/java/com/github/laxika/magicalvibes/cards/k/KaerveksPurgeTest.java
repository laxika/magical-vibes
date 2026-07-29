package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AdamantWill;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KaerveksPurgeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target creature with mana value X and deals its power to its controller")
    void destroysCreatureAndDealsPowerDamage() {
        harness.addToBattlefield(player2, new GrizzlyBears()); // mana value 2, 2/2
        UUID target = harness.getPermanentId(player2, "Grizzly Bears");
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.setHand(player1, List.of(new KaerveksPurge()));
        harness.addMana(player1, ManaColor.BLACK, 3); // {X=2}{B}{R}
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, 2, target);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Deals no damage when the creature survives destruction")
    void indestructibleCreatureTakesNoDamage() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID target = harness.getPermanentId(player2, "Grizzly Bears");
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.setHand(player1, List.of(new AdamantWill(), new KaerveksPurge()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target);
        harness.passBothPriorities();

        harness.castSorcery(player1, 0, 2, target);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Cannot target a creature whose mana value does not equal X")
    void cannotTargetCreatureWithDifferentManaValue() {
        harness.addToBattlefield(player2, new GrizzlyBears()); // mana value 2
        UUID target = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new KaerveksPurge()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 3, target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Plains());
        UUID target = harness.getPermanentId(player2, "Plains");

        harness.setHand(player1, List.of(new KaerveksPurge()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, target))
                .isInstanceOf(IllegalStateException.class);
    }
}
