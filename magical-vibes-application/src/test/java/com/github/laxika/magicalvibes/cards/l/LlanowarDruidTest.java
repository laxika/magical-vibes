package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LlanowarDruidTest extends BaseCardTest {

    @Test
    void untapsAllForestsIncludingOpponents() {
        Permanent druid = addDruid(player1);
        Permanent myForest = addTapped(player1, new Forest());
        Permanent myIsland = addTapped(player1, new Island());
        Permanent theirForest = addTapped(player2, new Forest());

        harness.activateAbility(player1, indexOf(player1, druid), null, null);
        harness.passBothPriorities();

        assertThat(myForest.isTapped()).isFalse();
        assertThat(theirForest.isTapped()).isFalse();
        assertThat(myIsland.isTapped()).isTrue();
    }

    @Test
    void sacrificesItselfAsCost() {
        Permanent druid = addDruid(player1);
        addTapped(player1, new Forest());

        harness.activateAbility(player1, indexOf(player1, druid), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(druid);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Llanowar Druid"));
    }

    private Permanent addDruid(Player player) {
        Permanent perm = new Permanent(new LlanowarDruid());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addTapped(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.tap();
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
