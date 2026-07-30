package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HolyJusticiarTest extends BaseCardTest {

    @Test
    @DisplayName("Taps a non-Zombie target and leaves it on the battlefield")
    void tapsNonZombie() {
        addJusticiar();
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getId().equals(bearsId))
                .singleElement()
                .matches(Permanent::isTapped);
    }

    @Test
    @DisplayName("Taps and exiles a Zombie target")
    void exilesZombie() {
        addJusticiar();
        harness.addToBattlefield(player2, new WalkingCorpse());
        UUID corpseId = harness.getPermanentId(player2, "Walking Corpse");
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, corpseId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(corpseId));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Walking Corpse"));
    }

    private void addJusticiar() {
        Permanent justiciar = harness.addToBattlefieldAndReturn(player1, new HolyJusticiar());
        justiciar.setSummoningSick(false);
    }
}
