package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.i.IslandSanctuary;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlickerOfFate.class, GrizzlyBears.class, Island.class, IslandSanctuary.class})
class FlickerOfFateTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles and immediately returns a target creature under its owner's control")
    void flickersTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FlickerOfFate()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(harness.getPermanentId(player2, "Grizzly Bears")).isNotEqualTo(bearsId);
    }

    @Test
    @DisplayName("Exiles and immediately returns a target enchantment")
    void flickersTargetEnchantment() {
        harness.addToBattlefield(player1, new IslandSanctuary());
        harness.setHand(player1, List.of(new FlickerOfFate()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID sanctuaryId = harness.getPermanentId(player1, "Island Sanctuary");

        harness.castInstant(player1, 0, sanctuaryId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Island Sanctuary");
        assertThat(harness.getPermanentId(player1, "Island Sanctuary")).isNotEqualTo(sanctuaryId);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new FlickerOfFate()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID islandId = harness.getPermanentId(player1, "Island");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, islandId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or enchantment");
    }
}
