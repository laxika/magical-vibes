package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrightstoneRitual.class, GoblinPiker.class, RagingGoblin.class})
class BrightstoneRitualTest extends BaseCardTest {

    @Test
    @DisplayName("Adds red mana for each Goblin on the battlefield")
    void addsRedManaForEachGoblinOnBattlefield() {
        harness.addToBattlefield(player1, new GoblinPiker());
        harness.addToBattlefield(player2, new RagingGoblin());
        harness.addToBattlefield(player2, new GoblinPiker());

        cast();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }

    @Test
    @DisplayName("Adds no mana when there are no Goblins on the battlefield")
    void addsNoManaWithoutGoblins() {
        cast();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    private void cast() {
        harness.setHand(player1, List.of(new BrightstoneRitual()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
