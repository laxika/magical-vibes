package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GomazoaTest extends BaseCardTest {

    @Test
    @DisplayName("Tucking Gomazoa also tucks each creature it is blocking")
    void tucksSourceAndBlockedCreature() {
        Permanent attacker = addCreatureReady(player1, new SuntailHawk());
        Permanent gomazoa = addCreatureReady(player2, new Gomazoa());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(gomazoa);
        assertThat(gd.playerDecks.get(player1.getId()).stream().map(card -> card.getName()))
                .contains("Suntail Hawk");
        assertThat(gd.playerDecks.get(player2.getId()).stream().map(card -> card.getName()))
                .contains("Gomazoa");
    }

    @Test
    @DisplayName("Gomazoa can be activated when it is not blocking")
    void tucksOnlyItselfWhenNotBlocking() {
        Permanent gomazoa = addCreatureReady(player1, new Gomazoa());
        Permanent otherCreature = addCreatureReady(player1, new LlanowarElves());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(gomazoa);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(otherCreature);
        assertThat(gd.playerDecks.get(player1.getId()).stream().map(card -> card.getName()))
                .contains("Gomazoa");
    }
}
