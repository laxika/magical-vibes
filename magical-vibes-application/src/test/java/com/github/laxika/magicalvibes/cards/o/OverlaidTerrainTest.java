package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OverlaidTerrainTest extends BaseCardTest {

    @Test
    @DisplayName("As it enters, sacrifices all lands you control")
    void asEntersSacrificesAllControlledLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());

        castAndResolveTerrain();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(p -> p.getCard().getName())
                .containsExactlyInAnyOrder("Grizzly Bears", "Overlaid Terrain");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Forest"))
                .hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(p -> p.getCard().getName())
                .containsExactly("Forest");
    }

    @Test
    @DisplayName("Overlaid Terrain grants controlled lands a two-mana any-color ability")
    void grantsTwoManaAnyColorAbilityToControlledLands() {
        harness.addToBattlefield(player1, new OverlaidTerrain());
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).get(1);

        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(forest.isTapped()).isTrue();
    }

    private void castAndResolveTerrain() {
        harness.setHand(player1, List.of(new OverlaidTerrain()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
    }
}
