package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeathPitOfferingTest extends BaseCardTest {

    // ===== ETB: sacrifice all creatures you control =====

    @Test
    @DisplayName("ETB sacrifices all creatures you control")
    void etbSacrificesAllYourCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        castAndResolveOffering();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears"))
                .hasSize(2);
    }

    @Test
    @DisplayName("ETB does not sacrifice creatures your opponent controls")
    void etbLeavesOpponentCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castAndResolveOffering();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    // ===== Static effect: +2/+2 to creatures you control =====

    @Test
    @DisplayName("Creatures you control get +2/+2")
    void buffsYourCreatures() {
        harness.addToBattlefield(player1, new DeathPitOffering());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not buff creatures your opponent controls")
    void doesNotBuffOpponentCreatures() {
        harness.addToBattlefield(player1, new DeathPitOffering());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent opponentBears = findPermanent(player2, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bonus is removed when Death Pit Offering leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new DeathPitOffering());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Death Pit Offering"));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    // ===== Helpers =====

    private void castAndResolveOffering() {
        harness.setHand(player1, List.of(new DeathPitOffering()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment spell
        harness.passBothPriorities(); // resolve ETB trigger
    }
}
