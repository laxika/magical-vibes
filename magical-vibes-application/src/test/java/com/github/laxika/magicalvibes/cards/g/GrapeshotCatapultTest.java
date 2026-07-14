package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GrapeshotCatapultTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to a target creature with flying")
    void dealsDamageToFlyingCreature() {
        Permanent catapult = addReadyCatapult(player1);

        harness.addToBattlefield(player2, new SuntailHawk());
        UUID hawkId = harness.getPermanentId(player2, "Suntail Hawk");

        harness.activateAbility(player1, 0, null, hawkId);
        harness.passBothPriorities();

        // 1 damage kills a 1/1 flier
        harness.assertInGraveyard(player2, "Suntail Hawk");
        assertThat(catapult.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetNonFlyingCreature() {
        addReadyCatapult(player1);

        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyCatapult(Player player) {
        GrapeshotCatapult card = new GrapeshotCatapult();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
