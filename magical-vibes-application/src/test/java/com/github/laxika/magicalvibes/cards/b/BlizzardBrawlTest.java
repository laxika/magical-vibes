package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlizzardBrawlTest extends BaseCardTest {

    @Test
    @DisplayName("Three snow permanents grant the pump and indestructible before the fight")
    void threeSnowPermanentsGrantBonusBeforeFight() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new SnowCoveredForest());
        harness.addToBattlefield(player1, new SnowCoveredForest());
        harness.addToBattlefield(player1, new SnowCoveredForest());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new BlizzardBrawl()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID hillGiantId = harness.getPermanentId(player1, "Hill Giant");
        UUID airElementalId = harness.getPermanentId(player2, "Air Elemental");
        harness.castSorcery(player1, 0, List.of(hillGiantId, airElementalId));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Fewer than three snow permanents do not grant the bonus")
    void fewerThanThreeSnowPermanentsDoNotGrantBonus() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new SnowCoveredForest());
        harness.addToBattlefield(player1, new SnowCoveredForest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new BlizzardBrawl()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID hillGiantId = harness.getPermanentId(player1, "Hill Giant");
        UUID airElementalId = harness.getPermanentId(player2, "Air Elemental");
        harness.castSorcery(player1, 0, List.of(hillGiantId, airElementalId));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("The first target must be a creature you control")
    void cannotTargetOpponentCreatureAsFirstTarget() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new BlizzardBrawl()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID airElementalId = harness.getPermanentId(player2, "Air Elemental");
        UUID hillGiantId = harness.getPermanentId(player1, "Hill Giant");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(airElementalId, hillGiantId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }
}
