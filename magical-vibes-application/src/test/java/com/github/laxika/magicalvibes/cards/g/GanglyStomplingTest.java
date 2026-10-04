package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FieldMarshal;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GanglyStompling.class, FieldMarshal.class})
class GanglyStomplingTest extends BaseCardTest {

    @Test
    @DisplayName("Changeling lets Gangly Stompling receive the Soldier lord's bonus")
    void changelingAndTrample() {
        harness.addToBattlefield(player1, new FieldMarshal());
        Permanent stompling = harness.addToBattlefieldAndReturn(player1, new GanglyStompling());

        assertThat(gqs.hasKeyword(gd, stompling, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, stompling)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, stompling)).isEqualTo(3);
    }

    @Test
    @DisplayName("Trample assigns excess damage to the defending player")
    void trampleDealsExcessDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new GanglyStompling());
        Permanent blocker = addCreatureReady(player2, new GanglyStompling());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 2
        ));

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Gangly Stompling");
        harness.assertInGraveyard(player2, "Gangly Stompling");
        harness.assertNotOnBattlefield(player1, "Gangly Stompling");
        harness.assertNotOnBattlefield(player2, "Gangly Stompling");
    }
}
