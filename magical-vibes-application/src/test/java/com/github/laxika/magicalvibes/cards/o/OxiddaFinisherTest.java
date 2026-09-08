package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.h.HauntedPlateMail;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OxiddaFinisherTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for Equipment reduces the generic mana cost")
    void affinityForEquipmentReducesGenericCost() {
        harness.addToBattlefield(player1, new HauntedPlateMail());
        harness.setHand(player1, List.of(new OxiddaFinisher()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Affinity counts only Equipment controlled by the spell's controller")
    void affinityCountsOnlyControlledEquipment() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player2, new HauntedPlateMail());
        harness.setHand(player1, List.of(new OxiddaFinisher()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
