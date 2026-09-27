package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpikedCorridorTorturePit.class, LightningBolt.class})
class SpikedCorridorTorturePitTest extends BaseCardTest {

    @Test
    void spikedCorridorCreatesThreeDevils() {
        castRoom(0);

        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(
                permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.DEVIL)).hasSize(3);
    }

    @Test
    void torturePitAddsTwoToNoncombatDamageToAnOpponent() {
        castRoom(1);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
    }

    private Permanent castRoom(int doorIndex) {
        harness.setHand(player1, List.of(new SpikedCorridorTorturePit()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castModalSorcery(player1, 0, doorIndex, List.of());
        harness.passBothPriorities();
        if (doorIndex == 0) {
            harness.passBothPriorities();
        }
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().contains("Spiked Corridor"))
                .findFirst().orElseThrow();
    }
}
