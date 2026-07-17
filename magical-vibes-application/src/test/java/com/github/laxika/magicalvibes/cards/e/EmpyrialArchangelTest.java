package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.v.VolcanicGeyser;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmpyrialArchangelTest extends BaseCardTest {

    @Test
    @DisplayName("Damage that would be dealt to the controller is redirected to the Archangel")
    void redirectsDamageFromControllerToItself() {
        harness.addToBattlefield(player2, new EmpyrialArchangel());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Controller takes no damage; the 5/8 Archangel absorbs 2 and survives.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Empyrial Archangel"));
    }

    @Test
    @DisplayName("Redirected damage meeting the Archangel's toughness destroys it, sparing the controller")
    void lethalRedirectedDamageDestroysArchangel() {
        harness.addToBattlefield(player2, new EmpyrialArchangel());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new VolcanicGeyser()));
        harness.addMana(player1, ManaColor.RED, 12);

        // 8 damage aimed at the controller is redirected to the 5/8 Archangel, destroying it.
        harness.castInstant(player1, 0, 8, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Empyrial Archangel"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Empyrial Archangel"));
    }
}
