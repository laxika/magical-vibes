package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FieldMarshal;
import com.github.laxika.magicalvibes.cards.p.PricklyBoggart;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GameTrailChangeling.class, FieldMarshal.class, PricklyBoggart.class})
class GameTrailChangelingTest extends BaseCardTest {

    @Test
    @DisplayName("Changeling gets boost from Field Marshal (Soldier lord) due to being every creature type")
    void changelingGetsSubtypeBoost() {
        harness.addToBattlefield(player1, new FieldMarshal());
        Permanent changeling = harness.addToBattlefieldAndReturn(player1, new GameTrailChangeling());

        assertThat(gqs.getEffectivePower(gd, changeling)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, changeling)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, changeling, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, changeling, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Trample deals excess combat damage to the defending player")
    void tramplesOverSmallBlocker() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new GameTrailChangeling());
        Permanent blocker = addCreatureReady(player2, new PricklyBoggart());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 3
        ));

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player2, "Prickly Boggart");
        harness.assertOnBattlefield(player1, "Game-Trail Changeling");
    }

    @Test
    @DisplayName("Trample deals no player damage when the blocker requires all combat damage")
    void noOverflowAgainstEqualSizedBlocker() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new GameTrailChangeling());
        Permanent blocker = addCreatureReady(player2, new GameTrailChangeling());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 4));

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Game-Trail Changeling");
        harness.assertInGraveyard(player2, "Game-Trail Changeling");
    }
}
