package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(HuntedPhantasm.class)
class HuntedPhantasmTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates five 1/1 red Goblin tokens under the targeted opponent's control")
    void etbCreatesGoblinTokensForTargetOpponent() {
        harness.setHand(player1, List.of(new HuntedPhantasm()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> goblins = findPermanents(player2, "Goblin");
        assertThat(goblins).hasSize(5);
        assertThat(findPermanents(player1, "Goblin")).isEmpty();

        for (Permanent goblin : goblins) {
            assertThat(goblin.getCard().isToken()).isTrue();
            assertThat(goblin.getCard().getPower()).isEqualTo(1);
            assertThat(goblin.getCard().getToughness()).isEqualTo(1);
            assertThat(goblin.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(goblin.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(goblin.getCard().getSubtypes()).containsExactly(CardSubtype.GOBLIN);
        }
    }

    @Test
    @DisplayName("Cannot target the controller with the ETB ability")
    void etbRequiresOpponentTarget() {
        harness.setHand(player1, List.of(new HuntedPhantasm()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Is unblockable")
    void isUnblockable() {
        harness.addToBattlefield(player1, new HuntedPhantasm());

        Permanent phantasm = findPermanent(player1, "Hunted Phantasm");

        assertThat(gqs.hasCantBeBlocked(gd, phantasm)).isTrue();
    }
}
