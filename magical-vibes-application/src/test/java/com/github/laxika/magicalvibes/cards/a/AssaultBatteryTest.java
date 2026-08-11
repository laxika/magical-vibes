package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssaultBatteryTest extends BaseCardTest {

    @Test
    @DisplayName("Assault deals 2 damage to a creature")
    void assaultDealsDamageToCreature() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new AssaultBattery()));
        harness.addMana(player1, ManaColor.RED, 1);

        castAssault(harness.getPermanentId(player2, "Air Elemental"));
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Air Elemental").getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Battery creates a 3/3 green Elephant token for its own cost")
    void batteryCreatesElephantToken() {
        harness.setHand(player1, List.of(new AssaultBattery()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        List<Permanent> elephants = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Elephant"))
                .toList();
        assertThat(elephants).hasSize(1);
        assertThat(elephants.getFirst().getCard().getPower()).isEqualTo(3);
        assertThat(elephants.getFirst().getCard().getToughness()).isEqualTo(3);
        assertThat(elephants.getFirst().getCard().getSubtypes()).contains(CardSubtype.ELEPHANT);
    }

    @Test
    @DisplayName("Assault cannot target a land")
    void assaultCannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new AssaultBattery()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> castAssault(harness.getPermanentId(player2, "Forest")))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castAssault(java.util.UUID targetId) {
        harness.ensurePriority(player1);
        gs.playCard(gd, player1, 0, 0, targetId, null);
    }
}
