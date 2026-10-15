package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AvariceTotem;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GripOfPhyresis.class, LeoninScimitar.class, AvariceTotem.class})
class GripOfPhyresisTest extends BaseCardTest {

    @Test
    @DisplayName("Gains control of an Equipment, creates a Germ, and attaches the Equipment to it")
    void gainsControlCreatesGermAndAttachesEquipment() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        castGripOfPhyresis(equipment);

        Permanent germ = findPermanent(player1, "Phyrexian Germ");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(equipment);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(equipment);
        assertThat(equipment.getAttachedTo()).isEqualTo(germ.getId());
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a non-Equipment artifact")
    void cannotTargetNonEquipmentArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AvariceTotem());
        harness.setHand(player1, List.of(new GripOfPhyresis()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Equipment");
    }

    private void castGripOfPhyresis(Permanent equipment) {
        harness.setHand(player1, List.of(new GripOfPhyresis()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, List.of(equipment.getId()));
    }
}
