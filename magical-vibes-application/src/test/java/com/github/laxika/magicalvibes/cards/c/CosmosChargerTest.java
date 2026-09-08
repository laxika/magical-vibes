package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AuguryRaven;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CosmosChargerTest extends BaseCardTest {

    @Test
    void reducesForetellCostAndAllowsForetellingOnAnOpponentsTurn() {
        Permanent charger = addCreatureReady(player1, new CosmosCharger());
        AuguryRaven raven = new AuguryRaven();
        harness.setHand(player1, List.of(raven));
        harness.forceActivePlayer(player2);
        harness.ensurePriority(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(raven.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(charger);
    }
}
