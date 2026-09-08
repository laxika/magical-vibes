package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeedshipImpact.class, FountainOfYouth.class, Bitterblossom.class, RodOfRuin.class, GrizzlyBears.class})
class SeedshipImpactTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a low-mana-value artifact and creates a Lander")
    void destroysLowManaValueArtifactAndCreatesLander() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        cast(target);

        harness.assertInGraveyard(player2, "Fountain of Youth");
        assertThat(findPermanents(player1, "Lander")).hasSize(1);
    }

    @Test
    @DisplayName("Destroys a low-mana-value enchantment and creates a Lander")
    void destroysLowManaValueEnchantmentAndCreatesLander() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Bitterblossom());

        cast(target);

        harness.assertInGraveyard(player2, "Bitterblossom");
        assertThat(findPermanents(player1, "Lander")).hasSize(1);
    }

    @Test
    @DisplayName("Does not create a Lander for a permanent with mana value greater than 2")
    void doesNotCreateLanderForHighManaValuePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());

        cast(target);

        harness.assertInGraveyard(player2, "Rod of Ruin");
        assertThat(findPermanents(player1, "Lander")).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareSeedshipImpact();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or enchantment");
    }

    private void cast(Permanent target) {
        prepareSeedshipImpact();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void prepareSeedshipImpact() {
        harness.setHand(player1, List.of(new SeedshipImpact()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
