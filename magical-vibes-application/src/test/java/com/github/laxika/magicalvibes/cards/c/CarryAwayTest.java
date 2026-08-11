package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CarryAwayTest extends BaseCardTest {

    @Test
    @DisplayName("Carry Away cannot target a non-Equipment permanent")
    void cannotTargetNonEquipment() {
        Permanent creature = addReadyPermanent(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CarryAway()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an Equipment");
    }

    @Test
    @DisplayName("Carry Away takes control of enchanted Equipment and unattaches it")
    void takesControlAndUnattachesEquipment() {
        Permanent creature = addReadyPermanent(player2, new GrizzlyBears());
        Permanent equipment = addReadyPermanent(player2, new LeoninScimitar());
        equipment.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new CarryAway()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, equipment.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(equipment.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(equipment.getId()));
        assertThat(equipment.getAttachedTo()).isNull();

        Permanent aura = findPermanent(player1, "Carry Away");
        assertThat(aura.getAttachedTo()).isEqualTo(equipment.getId());
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
