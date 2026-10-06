package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FireDiamond;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NevinyrralsDisk;
import com.github.laxika.magicalvibes.cards.n.NanoformSentinel;
import com.github.laxika.magicalvibes.cards.n.NutrientBlock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuinousRampage.class, FireDiamond.class, GrizzlyBears.class, NevinyrralsDisk.class,
        NanoformSentinel.class, NutrientBlock.class})
class RuinousRampageTest extends BaseCardTest {

    @Test
    @DisplayName("Damage mode deals 3 damage to each opponent, not the controller")
    void damageModeDealsDamageToEachOpponent() {
        harness.setHand(player1, List.of(new RuinousRampage()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Exile mode exiles artifacts with mana value 3 or less only")
    void exileModeExilesMatchingArtifacts() {
        harness.addToBattlefield(player1, new FireDiamond());
        harness.addToBattlefield(player2, new NevinyrralsDisk());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RuinousRampage()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fire Diamond");
        harness.assertOnBattlefield(player2, "Nevinyrral's Disk");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Exile mode includes mana value three and indestructible artifacts on both sides")
    void exileModeIncludesBoundaryAndIndestructibleArtifacts() {
        NutrientBlock food = new NutrientBlock();
        NanoformSentinel sentinel = new NanoformSentinel();
        harness.addToBattlefield(player1, food);
        harness.addToBattlefield(player2, sentinel);
        harness.setHand(player1, List.of(new RuinousRampage()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nutrient Block");
        harness.assertNotOnBattlefield(player2, "Nanoform Sentinel");
        assertThat(gd.findExiledCard(food.getId())).isNotNull();
        assertThat(gd.findExiledCard(sentinel.getId())).isNotNull();
        harness.assertNotInGraveyard(player1, "Nutrient Block");
        harness.assertNotInGraveyard(player2, "Nanoform Sentinel");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Damage mode leaves artifacts on both sides untouched")
    void damageModeDoesNotExileArtifacts() {
        harness.addToBattlefield(player1, new NutrientBlock());
        harness.addToBattlefield(player2, new NanoformSentinel());
        harness.setHand(player1, List.of(new RuinousRampage()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nutrient Block");
        harness.assertOnBattlefield(player2, "Nanoform Sentinel");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Exile mode can resolve with no artifacts and does not deal damage")
    void exileModeWithNoArtifacts() {
        harness.setHand(player1, List.of(new RuinousRampage()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ruinous Rampage");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
