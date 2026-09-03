package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FireDiamond;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NevinyrralsDisk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuinousRampage.class, FireDiamond.class, GrizzlyBears.class, NevinyrralsDisk.class})
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

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
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
}
