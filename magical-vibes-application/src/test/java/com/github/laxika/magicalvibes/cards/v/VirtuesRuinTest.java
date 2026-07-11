package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VirtuesRuinTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys white creatures controlled by both players")
    void destroysWhiteCreatures() {
        harness.addToBattlefield(player1, new EliteVanguard());
        harness.addToBattlefield(player2, new EliteVanguard());
        harness.setHand(player1, List.of(new VirtuesRuin()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Elite Vanguard"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Elite Vanguard"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Elite Vanguard"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Elite Vanguard"));
    }

    @Test
    @DisplayName("Leaves non-white creatures untouched")
    void leavesNonWhiteCreatures() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new VirtuesRuin()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Hill Giant"));
    }

    @Test
    @DisplayName("Destroys only white creatures among a mixed board")
    void destroysOnlyWhiteAmongMixed() {
        harness.addToBattlefield(player1, new EliteVanguard());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new VirtuesRuin()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Elite Vanguard"))
                .anyMatch(p -> p.getCard().getName().equals("Hill Giant"));
    }
}
