package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LayDownArmsTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature within the Plains count and its controller gains 3 life")
    void exilesCreatureAndGivesLifeToItsController() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        int lifeBefore = gd.getLife(player2.getId());

        castLayDownArms(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("Cannot target a creature whose mana value exceeds the Plains count")
    void cannotTargetCreatureAbovePlainsCount() {
        harness.addToBattlefield(player1, new Plains());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LayDownArms()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counts only Plains controlled by the spell's controller")
    void countsPlainsControlledByCaster() {
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new Plains());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LayDownArms()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castLayDownArms(Permanent target) {
        harness.setHand(player1, List.of(new LayDownArms()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
