package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SanitariumSkeletonTest extends BaseCardTest {

    @Test
    @DisplayName("Graveyard ability returns Sanitarium Skeleton to hand")
    void resolvingGraveyardAbilityReturnsToHand() {
        harness.setGraveyard(player1, List.of(new SanitariumSkeleton()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Sanitarium Skeleton");
        harness.assertNotInGraveyard(player1, "Sanitarium Skeleton");
    }

    @Test
    @DisplayName("Cannot activate graveyard ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        harness.setGraveyard(player1, List.of(new SanitariumSkeleton()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
