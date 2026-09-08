package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SiegeModificationTest extends BaseCardTest {

    @Test
    @DisplayName("Siege Modification makes a Vehicle a creature and gives it +3/+0 and first strike")
    void modifiesVehicle() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new SleekSchooner());

        castSiegeModification(vehicle);

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, vehicle)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Siege Modification gives an enchanted creature +3/+0 and first strike")
    void modifiesCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castSiegeModification(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Siege Modification cannot target a noncreature non-Vehicle permanent")
    void cannotTargetOtherPermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new SiegeModification()));
        addSiegeModificationMana();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or Vehicle");
    }

    private void castSiegeModification(Permanent target) {
        harness.setHand(player1, List.of(new SiegeModification()));
        addSiegeModificationMana();
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addSiegeModificationMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
