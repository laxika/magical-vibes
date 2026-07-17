package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DetonateTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target artifact with mana value X and deals X damage to its controller")
    void destroysArtifactAndDealsDamage() {
        harness.addToBattlefield(player2, new RodOfRuin()); // mana value 4
        UUID target = harness.getPermanentId(player2, "Rod of Ruin");
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.setHand(player1, List.of(new Detonate()));
        harness.addMana(player1, ManaColor.RED, 5); // {X=4}{R}
        harness.castSorcery(player1, 0, 4, target);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Rod of Ruin"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Rod of Ruin"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    @DisplayName("Deals damage even when the artifact cannot be destroyed")
    void dealsDamageWhenIndestructible() {
        harness.addToBattlefield(player2, new DarksteelPlate()); // indestructible, mana value 3
        UUID target = harness.getPermanentId(player2, "Darksteel Plate");
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.setHand(player1, List.of(new Detonate()));
        harness.addMana(player1, ManaColor.RED, 4); // {X=3}{R}
        harness.castSorcery(player1, 0, 3, target);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Darksteel Plate"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Cannot target an artifact whose mana value does not equal X")
    void cannotTargetArtifactWithDifferentManaValue() {
        harness.addToBattlefield(player2, new RodOfRuin()); // mana value 4
        UUID target = harness.getPermanentId(player2, "Rod of Ruin");

        harness.setHand(player1, List.of(new Detonate()));
        harness.addMana(player1, ManaColor.RED, 4); // {X=3}{R}

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 3, target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a nonartifact permanent")
    void cannotTargetNonartifact() {
        harness.addToBattlefield(player2, new GrizzlyBears()); // mana value 2, not an artifact
        UUID target = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new Detonate()));
        harness.addMana(player1, ManaColor.RED, 3); // {X=2}{R}

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, target))
                .isInstanceOf(IllegalStateException.class);
    }
}
