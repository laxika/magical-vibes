package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.Glimmerpost;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UncontrolledInfestation.class, Glimmerpost.class, Swamp.class})
class UncontrolledInfestationTest extends BaseCardTest {

    @Test
    @DisplayName("Can enchant a nonbasic land")
    void canEnchantNonbasicLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Glimmerpost());
        castAt(land);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot enchant a basic land")
    void cannotEnchantBasicLand() {
        harness.addToBattlefield(player2, new Glimmerpost());
        Permanent basicLand = harness.addToBattlefieldAndReturn(player2, new Swamp());
        harness.setHand(player1, List.of(new UncontrolledInfestation()));
        addMana();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, basicLand.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonbasic land");
    }

    @Test
    @DisplayName("Tapping the enchanted nonbasic land destroys it")
    void tappingEnchantedNonbasicLandDestroysIt() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Glimmerpost());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new UncontrolledInfestation());
        aura.setAttachedTo(land.getId());

        harness.tapPermanent(player2, 0);
        resolveStackFully();

        harness.assertNotOnBattlefield(player2, "Glimmerpost");
    }

    @Test
    @DisplayName("Tapping an unenchanted nonbasic land does not destroy it")
    void tappingUnenchantedNonbasicLandDoesNotDestroyIt() {
        harness.addToBattlefield(player2, new Glimmerpost());

        harness.tapPermanent(player2, 0);
        resolveStackFully();

        harness.assertOnBattlefield(player2, "Glimmerpost");
    }

    private void castAt(Permanent target) {
        harness.setHand(player1, List.of(new UncontrolledInfestation()));
        addMana();
        harness.castEnchantment(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void resolveStackFully() {
        for (int i = 0; i < 8 && (!gd.stack.isEmpty() || !gd.pendingManaAbilityTriggers.isEmpty()); i++) {
            harness.passBothPriorities();
        }
    }
}
