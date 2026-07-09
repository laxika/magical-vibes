package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HurlyBurlyTest extends BaseCardTest {

    @Test
    @DisplayName("Mode 0 deals 1 damage to each creature without flying only")
    void withoutFlyingModeHitsGroundCreatures() {
        harness.addToBattlefield(player1, new FugitiveWizard()); // 1/1 ground
        harness.addToBattlefield(player2, new SuntailHawk());    // 1/1 flying
        harness.setHand(player1, List.of(new HurlyBurly()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 0); // mode 0 = without flying
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Fugitive Wizard"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Suntail Hawk"));
    }

    @Test
    @DisplayName("Mode 1 deals 1 damage to each creature with flying only")
    void withFlyingModeHitsFliers() {
        harness.addToBattlefield(player1, new FugitiveWizard()); // 1/1 ground
        harness.addToBattlefield(player2, new SuntailHawk());    // 1/1 flying
        harness.setHand(player1, List.of(new HurlyBurly()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 1); // mode 1 = with flying
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Suntail Hawk"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Fugitive Wizard"));
    }

    @Test
    @DisplayName("Choosing an invalid mode is rejected at cast time")
    void invalidModeIsRejected() {
        harness.setHand(player1, List.of(new HurlyBurly()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 99))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid mode index");
    }
}
