package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WindingCanyons;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LlanowarDruid.class, Forest.class, WindingCanyons.class})
class LlanowarDruidTest extends BaseCardTest {

    @Test
    void untapsAllForestsIncludingOpponents() {
        Permanent druid = addDruid(player1);
        Permanent myForest = addTapped(player1, new Forest());
        Permanent myNonForestLand = addTapped(player1, new WindingCanyons());
        Permanent theirForest = addTapped(player2, new Forest());

        harness.activateAbility(player1, indexOf(player1, druid), null, null);
        harness.passBothPriorities();

        assertThat(myForest.isTapped()).isFalse();
        assertThat(theirForest.isTapped()).isFalse();
        assertThat(myNonForestLand.isTapped()).isTrue();
    }

    @Test
    void sacrificesItselfAsCost() {
        Permanent druid = addDruid(player1);

        harness.activateAbility(player1, indexOf(player1, druid), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(druid);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Llanowar Druid"));
    }

    @Test
    void cannotActivateWhenAlreadyTapped() {
        Permanent druid = addDruid(player1);
        druid.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, druid), null, null))
                .hasMessageContaining("already tapped");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(druid);
    }

    private Permanent addDruid(Player player) {
        return addCreatureReady(player, new LlanowarDruid());
    }

    private Permanent addTapped(Player player, Card card) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setSummoningSick(false);
        perm.tap();
        return perm;
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
