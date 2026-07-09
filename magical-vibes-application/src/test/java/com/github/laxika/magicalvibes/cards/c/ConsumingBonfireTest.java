package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BlackPoplarShaman;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConsumingBonfireTest extends BaseCardTest {

    private Permanent battlefieldPermanent(String cardName) {
        return gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals(cardName))
                .findFirst().orElseThrow();
    }

    @Nested
    @DisplayName("Mode 1: 4 damage to target non-Elemental creature")
    class NonElementalMode {

        @Test
        @DisplayName("Deals 4 damage to a non-Elemental creature")
        void deals4ToNonElemental() {
            harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2, not an Elemental

            harness.setHand(player1, List.of(new ConsumingBonfire()));
            harness.addMana(player1, ManaColor.RED, 5);

            harness.castSorcery(player1, 0, 0, battlefieldPermanent("Grizzly Bears").getId());
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Cannot target an Elemental creature")
        void cannotTargetElemental() {
            harness.addToBattlefield(player2, new AirElemental()); // Elemental

            harness.setHand(player1, List.of(new ConsumingBonfire()));
            harness.addMana(player1, ManaColor.RED, 5);

            var targetId = battlefieldPermanent("Air Elemental").getId();
            assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, targetId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 2: 7 damage to target Treefolk creature")
    class TreefolkMode {

        @Test
        @DisplayName("Deals 7 damage to a Treefolk creature")
        void deals7ToTreefolk() {
            harness.addToBattlefield(player2, new BlackPoplarShaman()); // 2/2 Treefolk

            harness.setHand(player1, List.of(new ConsumingBonfire()));
            harness.addMana(player1, ManaColor.RED, 5);

            harness.castSorcery(player1, 0, 1, battlefieldPermanent("Black Poplar Shaman").getId());
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Black Poplar Shaman"));
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Black Poplar Shaman"));
        }

        @Test
        @DisplayName("Cannot target a non-Treefolk creature")
        void cannotTargetNonTreefolk() {
            harness.addToBattlefield(player2, new GrizzlyBears());

            harness.setHand(player1, List.of(new ConsumingBonfire()));
            harness.addMana(player1, ManaColor.RED, 5);

            var targetId = battlefieldPermanent("Grizzly Bears").getId();
            assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, targetId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
