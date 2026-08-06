package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.StoneRain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DarksteelCitadelTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for mana adds {C}")
    void tapForColorlessMana() {
        harness.addToBattlefield(player1, new DarksteelCitadel());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Indestructible keeps it on the battlefield through a destroy effect")
    void survivesDestruction() {
        harness.addToBattlefield(player2, new DarksteelCitadel());
        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Darksteel Citadel");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Darksteel Citadel");
    }
}
