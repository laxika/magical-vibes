package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.j.JungleWeaver;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KederektCreeper.class, JungleWeaver.class})
class KederektCreeperTest extends BaseCardTest {

    @Test
    void menaceRejectsOneBlockerAndAllowsTwo() {
        addCreatureReady(player1, new KederektCreeper());
        Permanent first = addCreatureReady(player2, new JungleWeaver());
        Permanent second = addCreatureReady(player2, new JungleWeaver());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked except by two or more creatures");

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    @Test
    void canBeLeftUnblockedAndDealsNormalPlayerDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new KederektCreeper());
        addCreatureReady(player2, new JungleWeaver());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player1, "Kederekt Creeper");
        harness.assertOnBattlefield(player2, "Jungle Weaver");
    }

    @Test
    void oneDamageToEachBlockerDestroysBothDespiteTheirToughness() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new KederektCreeper());
        Permanent first = addCreatureReady(player2, new JungleWeaver());
        Permanent second = addCreatureReady(player2, new JungleWeaver());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(first.getId(), 1, second.getId(), 1));

        harness.assertNotOnBattlefield(player2, "Jungle Weaver");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Jungle Weaver")).hasSize(2);
        harness.assertInGraveyard(player1, "Kederekt Creeper");
        harness.assertLife(player2, 20);
    }

    @Test
    void blockerReceivingNoDamageSurvivesDeathtouch() {
        addCreatureReady(player1, new KederektCreeper());
        Permanent first = addCreatureReady(player2, new JungleWeaver());
        Permanent second = addCreatureReady(player2, new JungleWeaver());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(first.getId(), 2, second.getId(), 0));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(second).doesNotContain(first);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Jungle Weaver")).hasSize(1);
        harness.assertInGraveyard(player1, "Kederekt Creeper");
    }

    @Test
    void deathtouchAlsoDestroysLargerAttackerWhenBlocking() {
        addCreatureReady(player1, new JungleWeaver());
        addCreatureReady(player2, new KederektCreeper());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Jungle Weaver");
        harness.assertInGraveyard(player2, "Kederekt Creeper");
        harness.assertNotOnBattlefield(player1, "Jungle Weaver");
        harness.assertNotOnBattlefield(player2, "Kederekt Creeper");
    }
}
